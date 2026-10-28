package br.com.statezone.service;

import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.FaseEliminatoria;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.ConfrontoEliminatorioRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.ProcessamentoConfrontoPendenteRepository;
import br.com.statezone.service.ranking.RankingCacheService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartidaEncerradaProcessorTest {

    @Mock private StatsEngine statsEngine;
    @Mock private CleanSheetEngine cleanSheetEngine;
    @Mock private SuspensionEngine suspensionEngine;
    @Mock private RankingCacheService rankingCacheService;
    @Mock private BracketEngine bracketEngine;
    @Mock private BracketService bracketService;
    @Mock private ConfrontoEliminatorioRepository confrontoRepository;
    @Mock private ProcessamentoConfrontoPendenteRepository pendenteRepository;
    @Mock private PartidaRepository partidaRepository;

    private PartidaEncerradaProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PartidaEncerradaProcessor(
                statsEngine,
                cleanSheetEngine,
                suspensionEngine,
                rankingCacheService,
                bracketEngine,
                bracketService,
                confrontoRepository,
                pendenteRepository,
                partidaRepository,
                new SimpleMeterRegistry()
        );
    }

    @Test
    void processar_duasVezes_naoRepeteEstatisticas() {
        Partida partida = partida(1L, false);
        when(partidaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(partida));
        when(partidaRepository.findByIdParaProcessamento(1L)).thenReturn(Optional.of(partida));
        when(confrontoRepository.findConfrontoByPartidaId(1L)).thenReturn(Optional.empty());
        when(pendenteRepository.findByPartidaId(1L)).thenReturn(Optional.empty());

        processor.processar(1L);
        processor.processar(1L);

        verify(statsEngine, times(1)).process(partida);
        assertThat(partida.isEstatisticasProcessadas()).isTrue();
    }

    @Test
    void processar_wo_classificaVencedor() {
        Time visitante = new Time();
        visitante.setId(2L);
        Partida partida = partida(5L, true);
        partida.setStatus(StatusPartida.WO_VISITANTE);
        partida.setTimeVisitante(visitante);

        ConfrontoEliminatorio confronto = new ConfrontoEliminatorio();
        confronto.setJogoUnico(true);
        confronto.setPartidaIda(partida);
        FaseEliminatoria fase = new FaseEliminatoria();
        confronto.setFaseEliminatoria(fase);

        when(partidaRepository.findByIdWithLock(5L)).thenReturn(Optional.of(partida));
        when(confrontoRepository.findConfrontoByPartidaId(5L)).thenReturn(Optional.of(confronto));
        when(bracketEngine.resolverVencedor(confronto)).thenReturn(visitante);
        when(bracketEngine.propagarVencedor(confronto, visitante)).thenReturn(Optional.empty());
        when(pendenteRepository.findByPartidaId(5L)).thenReturn(Optional.empty());

        processor.processar(5L);

        assertThat(confronto.getStatusConfronto()).isEqualTo(StatusConfronto.ENCERRADO);
        assertThat(confronto.getTimeClassificado()).isEqualTo(visitante);
        verify(statsEngine, never()).process(partida);
        verify(bracketService).verificarFase(fase);
    }

    private Partida partida(Long id, boolean processada) {
        Campeonato campeonato = new Campeonato();
        campeonato.setId(9L);
        Partida partida = new Partida();
        partida.setId(id);
        partida.setCampeonato(campeonato);
        partida.setEstatisticasProcessadas(processada);
        return partida;
    }
}
