package br.com.statezone.service;

import br.com.statezone.dto.eventoPartida.EventoPartidaRequestDto;
import br.com.statezone.dto.eventoPartida.EventoPartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.mapper.EventoPartidaMapper;
import br.com.statezone.repository.EventoPartidaRepository;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.evento;
import static br.com.statezone.support.TestFixtures.jogador;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoPartidaServiceTest {

    @Mock
    private EventoPartidaRepository eventoPartidaRepository;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private JogadorRepository jogadorRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    private EventoPartidaService service;

    private final EventoPartidaMapper eventoPartidaMapper = Mappers.getMapper(EventoPartidaMapper.class);

    @BeforeEach
    void setUp() {
        service = new EventoPartidaService(
                eventoPartidaRepository,
                partidaRepository,
                jogadorRepository,
                eventoPartidaMapper,
                publisher
        );
    }

    @Test
    void registrarEvento_deveAtualizarPlacarERegistrarAssistencia() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador atacante = jogador(20L, mandante, "Atacante");
        Jogador assistente = jogador(21L, mandante, "Assistente");
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.AO_VIVO, 4, 0, 0);

        EventoPartidaRequestDto request = new EventoPartidaRequestDto(
                TipoEvento.GOL,
                12,
                null,
                "Gol",
                20L,
                21L,
                null
        );

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(jogadorRepository.findById(20L)).thenReturn(Optional.of(atacante));
        when(jogadorRepository.findById(21L)).thenReturn(Optional.of(assistente));
        when(eventoPartidaRepository.save(any(EventoPartida.class))).thenAnswer(invocation -> {
            EventoPartida evento = invocation.getArgument(0);
            evento.setId(200L);
            return evento;
        });
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventoPartidaResponseDto response = service.registrarEvento(request, 30L);

        assertThat(response.id()).isEqualTo(200L);
        assertThat(response.tipoEvento()).isEqualTo(TipoEvento.GOL);
        assertThat(response.jogadorId()).isEqualTo(20L);
        assertThat(response.assistenteId()).isEqualTo(21L);
        assertThat(response.partidaId()).isEqualTo(30L);
        assertThat(partida.getGolsMandante()).isEqualTo(1);
        assertThat(partida.getGolsVisitante()).isZero();

        ArgumentCaptor<EventoPartida> captor = ArgumentCaptor.forClass(EventoPartida.class);
        verify(eventoPartidaRepository).save(captor.capture());
        assertThat(captor.getValue().getJogador()).isSameAs(atacante);
        assertThat(captor.getValue().getJogadorSecundario()).isSameAs(assistente);
        assertThat(captor.getValue().getTime()).isSameAs(mandante);
    }

    @Test
    void registrarEvento_deveAnularGolViaVarEDescontarPlacar() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador atacante = jogador(20L, mandante, "Atacante");
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.AO_VIVO, 4, 1, 0);
        EventoPartida original = evento(100L, TipoEvento.GOL, 17, null, "Gol original", atacante, null, mandante, false);
        original.setPartida(partida);

        EventoPartidaRequestDto request = new EventoPartidaRequestDto(
                TipoEvento.VAR_GOL_ANULADO,
                18,
                null,
                "VAR",
                null,
                null,
                100L
        );

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(eventoPartidaRepository.findById(100L)).thenReturn(Optional.of(original));
        when(eventoPartidaRepository.save(any(EventoPartida.class))).thenAnswer(invocation -> {
            EventoPartida evento = invocation.getArgument(0);
            if (evento.getId() == null) {
                evento.setId(201L);
            }
            return evento;
        });
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventoPartidaResponseDto response = service.registrarEvento(request, 30L);

        assertThat(response.tipoEvento()).isEqualTo(TipoEvento.VAR_GOL_ANULADO);
        assertThat(response.eventoRelacionadoId()).isEqualTo(100L);
        assertThat(original.isAnulado()).isTrue();
        assertThat(partida.getGolsMandante()).isZero();

        ArgumentCaptor<EventoPartida> captor = ArgumentCaptor.forClass(EventoPartida.class);
        verify(eventoPartidaRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0)).isSameAs(original);
        assertThat(captor.getAllValues().get(1).getEventoRelacionado()).isSameAs(original);
        assertThat(captor.getAllValues().get(1).getTime()).isSameAs(mandante);
    }

    @Test
    void registrarEvento_deveRejeitarJogadorForaDaPartida() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Time outroTime = time(12L, "Outro");
        Jogador atacante = jogador(20L, outroTime, "Atacante");
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.AO_VIVO, 4, 0, 0);

        EventoPartidaRequestDto request = new EventoPartidaRequestDto(
                TipoEvento.GOL,
                12,
                null,
                "Gol",
                20L,
                null,
                null
        );

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(jogadorRepository.findById(20L)).thenReturn(Optional.of(atacante));

        assertThatThrownBy(() -> service.registrarEvento(request, 30L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Jogador não pertence aos times da partida");

        verify(eventoPartidaRepository, never()).save(any());
        verify(partidaRepository, never()).save(any());
    }

    @Test
    void registrarEvento_deveRejeitarEventoQueExigeJogadorSemInformar() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.AO_VIVO, 4, 0, 0);

        EventoPartidaRequestDto request = new EventoPartidaRequestDto(
                TipoEvento.GOL,
                12,
                null,
                "Gol",
                null,
                null,
                null
        );

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));

        assertThatThrownBy(() -> service.registrarEvento(request, 30L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Este tipo de evento exige um jogador");
    }
}
