package br.com.statezone.listeners;

import br.com.statezone.events.PartidaEncerradaEvent;
import br.com.statezone.model.Partida;
import br.com.statezone.model.ProcessamentoConfrontoPendente;
import br.com.statezone.repository.ProcessamentoConfrontoPendenteRepository;
import br.com.statezone.service.*;
import br.com.statezone.service.ranking.RankingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartidaEncerradaOrchestrator {

    private final StatsEngine statsEngine;
    private final CleanSheetEngine cleanSheetEngine;
    private final SuspensionEngine suspensionEngine;
    private final RankingCacheService rankingCacheService;
    private final BracketEngine bracketEngine;
    private final BracketService bracketService;
    private final br.com.statezone.repository.ConfrontoEliminatorioRepository confrontoRepository;
    private final ProcessamentoConfrontoPendenteRepository pendenteRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePartidaEncerrada(PartidaEncerradaEvent event) {
        Partida partida = event.getPartida();

        try {
            statsEngine.process(partida);
            cleanSheetEngine.process(partida);
            suspensionEngine.process(partida);
            rankingCacheService.recalcular(partida.getCampeonato().getId());

            confrontoRepository.findConfrontoByPartidaId(partida.getId())
                    .ifPresent(confronto -> {
                        if (deveEncerrarConfronto(confronto)) {
                            mapearPenaltisParaConfronto(confronto, partida);

                            var vencedor = bracketEngine.resolverVencedor(confronto);
                            confronto.setTimeClassificado(vencedor);
                            confronto.setStatusConfronto(br.com.statezone.enums.StatusConfronto.ENCERRADO);
                            confrontoRepository.save(confronto);

                            bracketEngine.propagarVencedor(confronto, vencedor)
                                    .ifPresent(confrontoRepository::save);

                            bracketService.verificarFase(confronto.getFaseEliminatoria());
                        }
                        pendenteRepository.findByPartidaId(partida.getId())
                                .ifPresent(p -> {
                                    p.setResolvido(true);
                                    pendenteRepository.save(p);
                                });
                    });
        } catch (Exception e) {
            log.error("Erro ao processar partida encerrada {}: {}", partida.getId(), e.getMessage(), e);
        }
    }

    private boolean deveEncerrarConfronto(br.com.statezone.model.ConfrontoEliminatorio c) {
        if (c.getJogoUnico() || c.getPartidaVolta() == null) {
            return c.getPartidaIda().getStatus() == br.com.statezone.enums.StatusPartida.ENCERRADA;
        }
        return c.getPartidaIda().getStatus() == br.com.statezone.enums.StatusPartida.ENCERRADA
                && c.getPartidaVolta().getStatus() == br.com.statezone.enums.StatusPartida.ENCERRADA;
    }

    private void mapearPenaltisParaConfronto(br.com.statezone.model.ConfrontoEliminatorio confronto, Partida partida) {
        if (partida.getGolsPenaltisMandante() == null) return;

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
