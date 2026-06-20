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
import br.com.statezone.repository.EstatisticasJogadorRepository;
import br.com.statezone.repository.JogadorRepository;
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
    private EstatisticasJogadorRepository estatisticasJogadorRepository;

    @Mock
    private EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;

    @Mock
    private JogadorRepository jogadorRepository;

    @Mock
    private EscalacaoPartidaRepository escalacaoPartidaRepository;

    private CleanSheetEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CleanSheetEngine(
                estatisticasJogadorRepository,
                estatisticasJogadorCampeonatoRepository,
                jogadorRepository,
                escalacaoPartidaRepository
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

        when(escalacaoPartidaRepository.findByPartidaIdWithJogador(30L)).thenReturn(escalacoes);
        when(estatisticasJogadorRepository.findByJogadorId(anyLong())).thenReturn(Optional.empty());
        when(estatisticasJogadorCampeonatoRepository.findByJogadorIdAndCampeonatoId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(estatisticasJogadorRepository.save(any(EstatisticasJogador.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(estatisticasJogadorCampeonatoRepository.save(any(EstatisticasJogadorCampeonato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        engine.process(partida);

        ArgumentCaptor<EstatisticasJogador> carreiraCaptor = ArgumentCaptor.forClass(EstatisticasJogador.class);
        verify(estatisticasJogadorRepository, times(2)).save(carreiraCaptor.capture());
        assertThat(carreiraCaptor.getAllValues())
                .extracting(stat -> stat.getJogador().getId(), EstatisticasJogador::getCleanSheets)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(20L, 1),
                        org.assertj.core.groups.Tuple.tuple(21L, 1)
                );

        ArgumentCaptor<EstatisticasJogadorCampeonato> campeonatoCaptor =
                ArgumentCaptor.forClass(EstatisticasJogadorCampeonato.class);
        verify(estatisticasJogadorCampeonatoRepository, times(2)).save(campeonatoCaptor.capture());
        assertThat(campeonatoCaptor.getAllValues())
                .extracting(stat -> stat.getJogador().getId(), EstatisticasJogadorCampeonato::getCleanSheets)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(20L, 1),
                        org.assertj.core.groups.Tuple.tuple(21L, 1)
                );
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
        when(estatisticasJogadorRepository.findByJogadorId(anyLong())).thenReturn(Optional.empty());
        when(estatisticasJogadorCampeonatoRepository.findByJogadorIdAndCampeonatoId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(estatisticasJogadorRepository.save(any(EstatisticasJogador.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(estatisticasJogadorCampeonatoRepository.save(any(EstatisticasJogadorCampeonato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        engine.process(partida);

        verify(estatisticasJogadorRepository, times(1)).save(any(EstatisticasJogador.class));
        verify(estatisticasJogadorCampeonatoRepository, times(1)).save(any(EstatisticasJogadorCampeonato.class));
    }
}
