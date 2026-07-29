package br.com.statezone.service;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.enums.Posicao;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.EscalacaoPartida;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.EscalacaoPartidaRepository;
import br.com.statezone.repository.EstatisticasJogadorCampeonatoRepository;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.service.helper.StatsHelper;
import br.com.statezone.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.escalacaoPartida;
import static br.com.statezone.support.TestFixtures.jogador;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class CleanSheetEngineTest {
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Mock
    private EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;

    @Mock
    private JogadorRepository jogadorRepository;

    @Mock
    private EscalacaoPartidaRepository escalacaoPartidaRepository;

    @Mock
    private StatsHelper statsHelper;

    private CleanSheetEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CleanSheetEngine(
                estatisticasJogadorCampeonatoRepository,
                jogadorRepository,
                escalacaoPartidaRepository,
                statsHelper
        );
    }

    @Test
    @WithMockUser
    void process_deveContarCleanSheetParaGoleirosEscalados() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador goleiroMandante = jogador(20L, mandante, "Goleiro A", Posicao.GOLEIRO, 1);
        Jogador goleiroVisitante = jogador(21L, visitante, "Goleiro B", Posicao.GOLEIRO, 1);
        Jogador zagueiro = jogador(22L, mandante, "Zagueiro", Posicao.ZAGUEIRO, 4);
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.ENCERRADA, 4, 0, 0);

        List<EscalacaoPartida> escalacoes = List.of(
                escalacaoPartida(1L, partida, goleiroMandante, null, Posicao.GOLEIRO, 1, true),
                escalacaoPartida(2L, partida, goleiroVisitante, null, Posicao.GOLEIRO, 1, true),
                escalacaoPartida(3L, partida, zagueiro, null, Posicao.ZAGUEIRO, 4, true)
        );

        EstatisticasJogador carreiraM = new EstatisticasJogador();
        carreiraM.setJogador(goleiroMandante);
        EstatisticasJogadorCampeonato campM = new EstatisticasJogadorCampeonato();
        campM.setJogador(goleiroMandante);
        campM.setCampeonato(campeonato);

        EstatisticasJogador carreiraV = new EstatisticasJogador();
        carreiraV.setJogador(goleiroVisitante);
        EstatisticasJogadorCampeonato campV = new EstatisticasJogadorCampeonato();
        campV.setJogador(goleiroVisitante);
        campV.setCampeonato(campeonato);

        when(escalacaoPartidaRepository.findByPartidaIdWithJogador(30L)).thenReturn(escalacoes);
        when(statsHelper.buscarOuCriarCarreira(goleiroMandante)).thenReturn(carreiraM);
        when(statsHelper.buscarOuCriarCarreira(goleiroVisitante)).thenReturn(carreiraV);
        when(statsHelper.obterOuCriarCampeonato(goleiroMandante, partida)).thenReturn(campM);
        when(statsHelper.obterOuCriarCampeonato(goleiroVisitante, partida)).thenReturn(campV);

        engine.process(partida);

        assertThat(carreiraM.getCleanSheets()).isEqualTo(1);
        assertThat(carreiraV.getCleanSheets()).isEqualTo(1);
        assertThat(campM.getCleanSheets()).isEqualTo(1);
        assertThat(campV.getCleanSheets()).isEqualTo(1);

        verify(statsHelper).salvarAmbos(carreiraM, campM);
        verify(statsHelper).salvarAmbos(carreiraV, campV);
    }

    @Test
    @WithMockUser
    void process_quandoNaoHaEscalacao_deveBuscarGoleiroNoRepositorio() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador goleiroMandante = jogador(20L, mandante, "Goleiro A", Posicao.GOLEIRO, 1);
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.ENCERRADA, 4, 0, 1);

        when(escalacaoPartidaRepository.findByPartidaIdWithJogador(30L)).thenReturn(List.of());
        when(jogadorRepository.findByTimeIdAndPosicao(10L, Posicao.GOLEIRO)).thenReturn(List.of(goleiroMandante));

        EstatisticasJogador carreira = new EstatisticasJogador();
        carreira.setJogador(goleiroMandante);
        EstatisticasJogadorCampeonato camp = new EstatisticasJogadorCampeonato();
        camp.setJogador(goleiroMandante);
        camp.setCampeonato(campeonato);
        when(statsHelper.buscarOuCriarCarreira(goleiroMandante)).thenReturn(carreira);
        when(statsHelper.obterOuCriarCampeonato(goleiroMandante, partida)).thenReturn(camp);

        engine.process(partida);

        assertThat(carreira.getCleanSheets()).isEqualTo(1);
        verify(statsHelper).salvarAmbos(carreira, camp);
    }
}
