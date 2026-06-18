package br.com.statezone.service.ranking;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.service.helper.ClassificacaoStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingEngineTest {

    @Mock
    private PartidaRepository partidaRepository;

    private RankingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RankingEngine(partidaRepository);
    }

    @Test
    void gerar_deveOrdenarPorPontosSaldoEGols() {
        var campeonato = campeonato(1L, 3);
        Time a = time(10L, "A");
        Time b = time(11L, "B");
        Time c = time(12L, "C");
        Time d = time(13L, "D");

        List<Partida> partidas = List.of(
                partida(1L, campeonato, a, b, StatusPartida.ENCERRADA, 1, 2, 0),
                partida(2L, campeonato, c, d, StatusPartida.ENCERRADA, 2, 1, 0),
                partida(3L, campeonato, a, c, StatusPartida.ENCERRADA, 3, 1, 1)
        );

        when(partidaRepository.findByCampeonatoIdAndStatusInWithTimes(eq(1L), anyList())).thenReturn(partidas);

        List<ClassificacaoStats> ranking = engine.gerar(1L);

        assertThat(ranking).hasSize(4);
        assertThat(ranking.get(0).getTimeId()).isEqualTo(10L);
        assertThat(ranking.get(0).getPontos()).isEqualTo(4);
        assertThat(ranking.get(0).getSaldoGols()).isEqualTo(2);
        assertThat(ranking.get(1).getTimeId()).isEqualTo(12L);
        assertThat(ranking.get(1).getPontos()).isEqualTo(4);
        assertThat(ranking.get(1).getSaldoGols()).isEqualTo(1);
        assertThat(ranking).allSatisfy(stats -> assertThat(stats.getPosicao()).isGreaterThan(0));
    }

    @Test
    void gerarPorTurno_deveFiltrarRodadasDoPrimeiroESegundoTurno() {
        var campeonato = campeonato(1L, 3);
        Time a = time(10L, "A");
        Time b = time(11L, "B");
        Time c = time(12L, "C");

        List<Partida> partidas = List.of(
                partida(1L, campeonato, a, b, StatusPartida.ENCERRADA, 1, 1, 0),
                partida(2L, campeonato, a, c, StatusPartida.ENCERRADA, 2, 1, 0),
                partida(3L, campeonato, b, a, StatusPartida.ENCERRADA, 3, 5, 0),
                partida(4L, campeonato, c, a, StatusPartida.ENCERRADA, 4, 5, 0)
        );

        when(partidaRepository.findByCampeonatoIdAndStatusInWithTimes(eq(1L), anyList())).thenReturn(partidas);
        when(partidaRepository.findMaxRodada(1L)).thenReturn(4);

        List<ClassificacaoStats> primeiroTurno = engine.gerarPorTurno(1L, 1);
        List<ClassificacaoStats> segundoTurno = engine.gerarPorTurno(1L, 2);

        assertThat(primeiroTurno).extracting(ClassificacaoStats::getTimeId, ClassificacaoStats::getPontos)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(10L, 6),
                        org.assertj.core.groups.Tuple.tuple(11L, 0),
                        org.assertj.core.groups.Tuple.tuple(12L, 0)
                );

        assertThat(segundoTurno).extracting(ClassificacaoStats::getTimeId, ClassificacaoStats::getPontos)
                .contains(
                        org.assertj.core.groups.Tuple.tuple(11L, 3),
                        org.assertj.core.groups.Tuple.tuple(12L, 3),
                        org.assertj.core.groups.Tuple.tuple(10L, 0)
                );
    }

    @Test
    void gerarPorGrupo_deveUsarPartidasDoGrupo() {
        var campeonato = campeonato(1L, 3);
        Time a = time(10L, "A");
        Time b = time(11L, "B");
        Partida partidaGrupo = partida(1L, campeonato, a, b, StatusPartida.ENCERRADA, 1, 1, 0);

        when(partidaRepository.findByGrupoIdAndStatusIn(5L, List.of(
                StatusPartida.ENCERRADA,
                StatusPartida.WO_MANDANTE,
                StatusPartida.WO_VISITANTE
        ))).thenReturn(List.of(partidaGrupo));

        List<ClassificacaoStats> ranking = engine.gerarPorGrupo(5L);

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).getTimeId()).isEqualTo(10L);
        verify(partidaRepository).findByGrupoIdAndStatusIn(5L, List.of(
                StatusPartida.ENCERRADA,
                StatusPartida.WO_MANDANTE,
                StatusPartida.WO_VISITANTE
        ));
    }
}
