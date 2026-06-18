package br.com.statezone.service;

import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.events.EventoPartidaCriadaEvent;
import br.com.statezone.events.PartidaEncerradaEvent;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.ConfrontoEliminatorioRepository;
import br.com.statezone.repository.EventoPartidaRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.TimeRepository;
import br.com.statezone.mapper.PartidaMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartidaServiceTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private PartidaWebSocketService partidaWebSocketService;

    @Mock
    private ConfrontoEliminatorioRepository confrontoEliminatorioRepository;

    @Mock
    private EventoPartidaRepository eventoPartidaRepository;

    private PartidaService service;

    private final PartidaMapper partidaMapper = Mappers.getMapper(PartidaMapper.class);

    @BeforeEach
    void setUp() {
        service = new PartidaService(
                partidaRepository,
                campeonatoRepository,
                timeRepository,
                partidaMapper,
                publisher,
                partidaWebSocketService,
                confrontoEliminatorioRepository,
                eventoPartidaRepository
        );
    }

    @Test
    void criar_devePersistirPartidaComTimesECampeonato() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        PartidaRequestDto request = new PartidaRequestDto(
                "Arena",
                "Arbitro",
                4,
                LocalDateTime.now().plusDays(1),
                StatusPartida.AGENDADA,
                null,
                null,
                1L,
                10L,
                11L
        );

        when(campeonatoRepository.findById(1L)).thenReturn(Optional.of(campeonato));
        when(timeRepository.findById(10L)).thenReturn(Optional.of(mandante));
        when(timeRepository.findById(11L)).thenReturn(Optional.of(visitante));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartidaResponseDto response = service.criar(request);

        assertThat(response.campeonatoId()).isEqualTo(1L);
        assertThat(response.timeMandanteId()).isEqualTo(10L);
        assertThat(response.timeVisitanteId()).isEqualTo(11L);
        assertThat(response.golsMandante()).isZero();
        assertThat(response.golsVisitante()).isZero();

        ArgumentCaptor<Partida> captor = ArgumentCaptor.forClass(Partida.class);
        verify(partidaRepository).save(captor.capture());
        assertThat(captor.getValue().getCampeonato()).isSameAs(campeonato);
        assertThat(captor.getValue().getTimeMandante()).isSameAs(mandante);
        assertThat(captor.getValue().getTimeVisitante()).isSameAs(visitante);
    }

    @Test
    void criar_deveRejeitarTimesIguais() {
        Campeonato campeonato = campeonato(1L, 3);
        Time time = time(10L, "Mesma equipe");
        PartidaRequestDto request = new PartidaRequestDto(
                "Arena",
                "Arbitro",
                4,
                LocalDateTime.now().plusDays(1),
                StatusPartida.AGENDADA,
                null,
                null,
                1L,
                10L,
                10L
        );

        when(campeonatoRepository.findById(1L)).thenReturn(Optional.of(campeonato));
        when(timeRepository.findById(10L)).thenReturn(Optional.of(time));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Times não podem ser iguais");

        verify(partidaRepository, never()).save(any());
    }

    @Test
    void iniciar_deveAlterarStatusCriarEventoESinalizarWebsocket() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.AGENDADA, 4, 0, 0);

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventoPartidaRepository.save(any(EventoPartida.class))).thenAnswer(invocation -> {
            EventoPartida evento = invocation.getArgument(0);
            evento.setId(500L);
            return evento;
        });

        PartidaResponseDto response = service.iniciar(30L);

        assertThat(response.status()).isEqualTo(StatusPartida.AO_VIVO);

        ArgumentCaptor<EventoPartida> eventoCaptor = ArgumentCaptor.forClass(EventoPartida.class);
        verify(eventoPartidaRepository).save(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getTipoEvento()).isEqualTo(TipoEvento.INICIO_PRIMEIRO_TEMPO);
        assertThat(eventoCaptor.getValue().getMinuto()).isEqualTo(1);
        assertThat(eventoCaptor.getValue().getDescricao()).isEqualTo("INICIO_PRIMEIRO_TEMPO");
        assertThat(eventoCaptor.getValue().getPartida()).isSameAs(partida);

        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOf(EventoPartidaCriadaEvent.class);

        ArgumentCaptor<PartidaResponseDto> wsCaptor = ArgumentCaptor.forClass(PartidaResponseDto.class);
        verify(partidaWebSocketService).notificarAtualizacaoPartida(wsCaptor.capture());
        assertThat(wsCaptor.getValue().status()).isEqualTo(StatusPartida.AO_VIVO);
    }

    @Test
    void encerrar_deveFinalizarPublicarEventosENotificarWebsocket() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Partida partida = partida(31L, campeonato, mandante, visitante, StatusPartida.AO_VIVO, 4, 1, 0);

        when(partidaRepository.findById(31L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventoPartidaRepository.save(any(EventoPartida.class))).thenAnswer(invocation -> {
            EventoPartida evento = invocation.getArgument(0);
            evento.setId(600L);
            return evento;
        });

        PartidaResponseDto response = service.encerrar(31L);

        assertThat(response.status()).isEqualTo(StatusPartida.ENCERRADA);

        ArgumentCaptor<EventoPartida> eventoCaptor = ArgumentCaptor.forClass(EventoPartida.class);
        verify(eventoPartidaRepository).save(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getTipoEvento()).isEqualTo(TipoEvento.FIM_PARTIDA);
        assertThat(eventoCaptor.getValue().getMinuto()).isEqualTo(90);

        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(publisher, times(2)).publishEvent(published.capture());
        assertThat(published.getAllValues())
                .hasSize(2)
                .anySatisfy(event -> assertThat(event).isInstanceOf(EventoPartidaCriadaEvent.class))
                .anySatisfy(event -> assertThat(event).isInstanceOf(PartidaEncerradaEvent.class));

        verify(partidaWebSocketService).notificarAtualizacaoPartida(any());
    }

    @Test
    void deletar_deveBloquearQuandoVinculadaAConfronto() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Partida partida = partida(32L, campeonato, mandante, visitante);
        ConfrontoEliminatorio confronto = new ConfrontoEliminatorio();
        confronto.setId(88L);

        when(partidaRepository.findById(32L)).thenReturn(Optional.of(partida));
        when(confrontoEliminatorioRepository.findConfrontoByPartidaId(32L)).thenReturn(Optional.of(confronto));

        assertThatThrownBy(() -> service.deletar(32L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Não é possível excluir uma partida vinculada a um confronto eliminatório");

        verify(partidaRepository, never()).delete(any());
    }

    @Test
    void iniciarPenaltis_e_encerrarComPenaltis_deveSalvarPlacarDosPenaltis() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Partida partida = partida(33L, campeonato, mandante, visitante, StatusPartida.AO_VIVO, 4, 1, 1);

        when(partidaRepository.findById(33L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventoPartidaRepository.save(any(EventoPartida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PartidaResponseDto emPenaltis = service.iniciarPenaltis(33L);
        assertThat(emPenaltis.status()).isEqualTo(StatusPartida.PENALTIS);
        assertThat(partida.getStatus()).isEqualTo(StatusPartida.PENALTIS);
        assertThat(partida.getGolsPenaltisMandante()).isZero();
        assertThat(partida.getGolsPenaltisVisitante()).isZero();

        PartidaResponseDto encerrada = service.encerrarComPenaltis(33L, 5, 4);
        assertThat(encerrada.status()).isEqualTo(StatusPartida.ENCERRADA);
        assertThat(partida.getStatus()).isEqualTo(StatusPartida.ENCERRADA);
        assertThat(partida.getGolsPenaltisMandante()).isEqualTo(5);
        assertThat(partida.getGolsPenaltisVisitante()).isEqualTo(4);

        verify(partidaRepository, times(2)).save(any(Partida.class));
    }

    @Test
    void encerrarComPenaltis_naoDeveAceitarEmpate() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Partida partida = partida(34L, campeonato, mandante, visitante, StatusPartida.PENALTIS, 4, 1, 1);

        when(partidaRepository.findById(34L)).thenReturn(Optional.of(partida));

        assertThatThrownBy(() -> service.encerrarComPenaltis(34L, 4, 4))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Pênaltis não podem empatar");
    }
}
