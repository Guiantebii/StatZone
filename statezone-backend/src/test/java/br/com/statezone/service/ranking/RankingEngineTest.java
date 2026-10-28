package br.com.statezone.service.ranking;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.service.helper.ClassificacaoStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingEngineTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Test
    void woVisitante_contaComoVitoriaPor3A0() {
        var camp = campeonato(1L, 3);
        var mandante = time(10L, "Mandante");
        var visitante = time(11L, "Visitante");
        var jogo = partida(30L, camp, mandante, visitante, StatusPartida.WO_VISITANTE, 1, 0, 3);
        when(partidaRepository.findByCampeonatoIdAndStatusInWithTimes(
                1L,
                List.of(StatusPartida.ENCERRADA, StatusPartida.WO_MANDANTE, StatusPartida.WO_VISITANTE)
        )).thenReturn(List.of(jogo));

        List<ClassificacaoStats> tabela = new RankingEngine(partidaRepository).gerar(1L);

        ClassificacaoStats statsVisitante = tabela.stream().filter(s -> s.getTimeId().equals(11L)).findFirst().orElseThrow();
        ClassificacaoStats statsMandante = tabela.stream().filter(s -> s.getTimeId().equals(10L)).findFirst().orElseThrow();
        assertThat(statsVisitante.getPontos()).isEqualTo(3);
        assertThat(statsVisitante.getVitorias()).isEqualTo(1);
        assertThat(statsVisitante.getGolsFeitos()).isEqualTo(3);
        assertThat(statsMandante.getDerrotas()).isEqualTo(1);
        assertThat(statsMandante.getPontos()).isZero();
    }
}
