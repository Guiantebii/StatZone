package br.com.statezone.listeners;

import br.com.statezone.repository.ProcessamentoConfrontoPendenteRepository;
import br.com.statezone.service.BracketEngine;
import br.com.statezone.service.BracketService;
import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.events.PartidaEncerradaEvent;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.ConfrontoEliminatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ConfrontoEventListener {

    private final BracketEngine bracketEngine;
    private final BracketService bracketService;
    private final ConfrontoEliminatorioRepository confrontoRepository;
    private final ProcessamentoConfrontoPendenteRepository pendenteRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePartidaEncerrada(PartidaEncerradaEvent event) {
        Partida partida = event.getPartida();
        processarConfronto(partida);
    }

    private void mapearPenaltisParaConfronto(ConfrontoEliminatorio confronto, Partida partida) {
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

    private boolean deveEncerrarConfronto(ConfrontoEliminatorio c) {
        if (c.getJogoUnico() || c.getPartidaVolta() == null) {
            return c.getPartidaIda().getStatus() == StatusPartida.ENCERRADA;
        }
        return c.getPartidaIda().getStatus() == StatusPartida.ENCERRADA
                && c.getPartidaVolta().getStatus() == StatusPartida.ENCERRADA;
    }

    public void processarConfronto(Partida partida) {
        confrontoRepository.findConfrontoByPartidaId(partida.getId())
                .ifPresent(confronto -> {
                    if (deveEncerrarConfronto(confronto)) {
                        mapearPenaltisParaConfronto(confronto, partida);

                        Time vencedor = bracketEngine.resolverVencedor(confronto);
                        confronto.setTimeClassificado(vencedor);
                        confronto.setStatusConfronto(StatusConfronto.ENCERRADO);
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
    }
}