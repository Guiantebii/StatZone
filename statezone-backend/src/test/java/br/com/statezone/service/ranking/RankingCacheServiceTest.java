package br.com.statezone.service.ranking;

import br.com.statezone.service.helper.ClassificacaoStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingCacheServiceTest {

    @Mock
    private RankingEngine rankingEngine;

    private RankingCacheService rankingCacheService;

    @BeforeEach
    void setUp() {
        rankingCacheService = new RankingCacheService(rankingEngine);
    }

    @Test
    void getRanking_deveChamarEngineNaPrimeiraVez() {
        List<ClassificacaoStats> expected = List.of(mock(ClassificacaoStats.class));
        when(rankingEngine.gerar(1L)).thenReturn(expected);

        List<ClassificacaoStats> result = rankingCacheService.getRanking(1L);

        assertThat(result).isSameAs(expected);
        verify(rankingEngine).gerar(1L);
    }

    @Test
    void getRanking_deveRetornarCacheadoNaSegundaVez() {
        List<ClassificacaoStats> expected = List.of(mock(ClassificacaoStats.class));
        when(rankingEngine.gerar(1L)).thenReturn(expected);

        List<ClassificacaoStats> first = rankingCacheService.getRanking(1L);
        List<ClassificacaoStats> second = rankingCacheService.getRanking(1L);

        assertThat(second).isSameAs(first);
        verify(rankingEngine, times(1)).gerar(1L);
    }

    @Test
    void getRankingPorTurno_deveChamarEngineNaPrimeiraVez() {
        List<ClassificacaoStats> expected = List.of(mock(ClassificacaoStats.class));
        when(rankingEngine.gerarPorTurno(1L, 1)).thenReturn(expected);

        List<ClassificacaoStats> result = rankingCacheService.getRankingPorTurno(1L, 1);

        assertThat(result).isSameAs(expected);
        verify(rankingEngine).gerarPorTurno(1L, 1);
    }

    @Test
    void getRankingPorGrupo_deveChamarEngineNaPrimeiraVez() {
        List<ClassificacaoStats> expected = List.of(mock(ClassificacaoStats.class));
        when(rankingEngine.gerarPorGrupo(1L)).thenReturn(expected);

        List<ClassificacaoStats> result = rankingCacheService.getRankingPorGrupo(1L);

        assertThat(result).isSameAs(expected);
        verify(rankingEngine).gerarPorGrupo(1L);
    }

    @Test
    void recalcular_deveAtualizarCache() {
        List<ClassificacaoStats> oldRanking = List.of(mock(ClassificacaoStats.class));
        List<ClassificacaoStats> newRanking = List.of(mock(ClassificacaoStats.class));
        when(rankingEngine.gerar(1L)).thenReturn(oldRanking, newRanking);

        rankingCacheService.getRanking(1L);
        rankingCacheService.recalcular(1L);

        List<ClassificacaoStats> result = rankingCacheService.getRanking(1L);
        assertThat(result).isSameAs(newRanking);
        verify(rankingEngine, times(2)).gerar(1L);
    }

    @Test
    void recalcular_deveRemoverApenasEntradasDoCampeonato() {
        when(rankingEngine.gerar(1L)).thenReturn(List.of(mock(ClassificacaoStats.class)));
        when(rankingEngine.gerar(2L)).thenReturn(List.of(mock(ClassificacaoStats.class)));
        when(rankingEngine.gerarPorGrupo(99L)).thenReturn(List.of(mock(ClassificacaoStats.class)));

        rankingCacheService.getRanking(1L);
        rankingCacheService.getRanking(2L);
        rankingCacheService.getRankingPorGrupo(99L);

        rankingCacheService.recalcular(1L);

        verify(rankingEngine, times(1)).gerar(2L);
        verify(rankingEngine, times(1)).gerarPorGrupo(99L);
    }

    @Test
    void getRanking_quandoCacheCheio_deveLimparEChamarEngineNovamente() throws Exception {
        when(rankingEngine.gerar(anyLong())).thenReturn(List.of(mock(ClassificacaoStats.class)));

        for (long i = 0; i < 200; i++) {
            rankingCacheService.getRanking(i);
        }

        verify(rankingEngine, times(200)).gerar(anyLong());

        rankingCacheService.getRanking(0L);

        verify(rankingEngine, times(201)).gerar(anyLong());
    }

    @Test
    void getRankingPorTurno_deveRetornarCacheado() {
        List<ClassificacaoStats> expected = List.of(mock(ClassificacaoStats.class));
        when(rankingEngine.gerarPorTurno(1L, 1)).thenReturn(expected);

        rankingCacheService.getRankingPorTurno(1L, 1);
        rankingCacheService.getRankingPorTurno(1L, 1);

        verify(rankingEngine, times(1)).gerarPorTurno(1L, 1);
    }

    @Test
    void getRankingPorGrupo_deveRetornarCacheado() {
        List<ClassificacaoStats> expected = List.of(mock(ClassificacaoStats.class));
        when(rankingEngine.gerarPorGrupo(1L)).thenReturn(expected);

        rankingCacheService.getRankingPorGrupo(1L);
        rankingCacheService.getRankingPorGrupo(1L);

        verify(rankingEngine, times(1)).gerarPorGrupo(1L);
    }

    @Test
    void getRankingPorTurno_e_Grupo_devemTerCachesSeparados() {
        when(rankingEngine.gerarPorTurno(1L, 1)).thenReturn(List.of(mock(ClassificacaoStats.class)));
        when(rankingEngine.gerarPorGrupo(1L)).thenReturn(List.of(mock(ClassificacaoStats.class)));

        rankingCacheService.getRankingPorTurno(1L, 1);
        rankingCacheService.getRankingPorGrupo(1L);

        rankingCacheService.getRankingPorTurno(1L, 1);
        rankingCacheService.getRankingPorGrupo(1L);

        verify(rankingEngine, times(1)).gerarPorTurno(1L, 1);
        verify(rankingEngine, times(1)).gerarPorGrupo(1L);
    }
}
