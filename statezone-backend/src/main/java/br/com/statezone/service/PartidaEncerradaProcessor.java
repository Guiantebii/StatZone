package br.com.statezone.service;

import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.Partida;
import br.com.statezone.repository.ConfrontoEliminatorioRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.ProcessamentoConfrontoPendenteRepository;
import br.com.statezone.service.ranking.RankingCacheService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartidaEncerradaProcessor {

    private final StatsEngine statsEngine;
    private final CleanSheetEngine cleanSheetEngine;
    private final SuspensionEngine suspensionEngine;
    private final RankingCacheService rankingCacheService;
    private final BracketEngine bracketEngine;
    private final BracketService bracketService;
    private final ConfrontoEliminatorioRepository confrontoRepository;
    private final ProcessamentoConfrontoPendenteRepository pendenteRepository;
    private final PartidaRepository partidaRepository;
    private final MeterRegistry meterRegistry;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processar(Long partidaId) {
        Partida bloqueada = partidaRepository.findByIdWithLock(partidaId)
                .orElseThrow(() -> new ResourceNotFoundException("Partida não encontrada"));

        if (!bloqueada.isEstatisticasProcessadas()) {
            reaplicarEstatisticas(partidaId);
            meterRegistry.counter("statzone.partidas.encerradas").increment();
        }

        confrontoRepository.findConfrontoByPartidaId(partidaId)
                .ifPresent(confronto -> encerrarConfrontoSeCompleto(confronto, bloqueada));

        pendenteRepository.findByPartidaId(partidaId).ifPresent(pendente -> {
            pendente.setResolvido(true);
            pendente.setUltimoErro(null);
            pendenteRepository.save(pendente);
        });
    }

    @Transactional
    public void reaplicarEstatisticas(Long partidaId) {
        Partida completa = partidaRepository.findByIdParaProcessamento(partidaId)
                .orElseThrow(() -> new ResourceNotFoundException("Partida não encontrada"));
        statsEngine.process(completa);
        cleanSheetEngine.process(completa);
        suspensionEngine.process(completa);
        rankingCacheService.recalcular(completa.getCampeonato().getId());
        completa.setEstatisticasProcessadas(true);
        partidaRepository.save(completa);
    }

    private void encerrarConfrontoSeCompleto(ConfrontoEliminatorio confronto, Partida partida) {
        if (!deveEncerrarConfronto(confronto)) {
            return;
        }

        mapearPenaltisParaConfronto(confronto, partida);

        var vencedor = bracketEngine.resolverVencedor(confronto);
        confronto.setTimeClassificado(vencedor);
        confronto.setStatusConfronto(StatusConfronto.ENCERRADO);
        confrontoRepository.save(confronto);

        bracketEngine.propagarVencedor(confronto, vencedor)
                .ifPresent(confrontoRepository::save);

        bracketService.verificarFase(confronto.getFaseEliminatoria());
    }

    private boolean deveEncerrarConfronto(ConfrontoEliminatorio confronto) {
        if (confronto.getJogoUnico() || confronto.getPartidaVolta() == null) {
            return partidaFinalizada(confronto.getPartidaIda());
        }
        return partidaFinalizada(confronto.getPartidaIda())
                && partidaFinalizada(confronto.getPartidaVolta());
    }

    private boolean partidaFinalizada(Partida partida) {
        if (partida == null || partida.getStatus() == null) {
            return false;
        }
        return partida.getStatus() == StatusPartida.ENCERRADA
                || partida.getStatus() == StatusPartida.WO_MANDANTE
                || partida.getStatus() == StatusPartida.WO_VISITANTE;
    }

    private void mapearPenaltisParaConfronto(ConfrontoEliminatorio confronto, Partida partida) {
        if (partida.getGolsPenaltisMandante() == null) {
            return;
        }

        boolean ehVolta = confronto.getPartidaVolta() != null
                && confronto.getPartidaVolta().getId().equals(partida.getId());

        if (ehVolta) {
            confronto.setGolsPenaltisA(partida.getGolsPenaltisVisitante());
            confronto.setGolsPenaltisB(partida.getGolsPenaltisMandante());
        } else {
            confronto.setGolsPenaltisA(partida.getGolsPenaltisMandante());
            confronto.setGolsPenaltisB(partida.getGolsPenaltisVisitante());
        }
    }
}
