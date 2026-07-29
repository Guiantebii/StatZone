package br.com.statezone.service;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.*;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.helper.CampeonatoAccessHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.Optional;

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class PartidaServiceTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private ConfrontoEliminatorioRepository confrontoEliminatorioRepository;

    @Mock
    private CampeonatoAccessHelper campeonatoAccessHelper;

    private PartidaService service;

    private final PartidaMapper partidaMapper = Mappers.getMapper(PartidaMapper.class);

    @BeforeEach
    void setUp() {
        service = new PartidaService(
                partidaRepository,
                campeonatoRepository,
                timeRepository,
                partidaMapper,
                confrontoEliminatorioRepository,
                campeonatoAccessHelper
        );
    }

    @Test
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
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
}
