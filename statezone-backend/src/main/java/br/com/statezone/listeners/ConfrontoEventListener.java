package br.com.statezone.listeners;

import br.com.statezone.events.PartidaEncerradaEvent;
import br.com.statezone.model.Partida;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfrontoEventListener {

    private final PartidaEncerradaOrchestrator orchestrator;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePartidaEncerrada(PartidaEncerradaEvent event) {
        log.warn("ConfrontoEventListener chamado diretamente - delegando para PartidaEncerradaOrchestrator");
        orchestrator.handlePartidaEncerrada(event);
    }

    public void processarConfronto(Partida partida) {
        log.warn("ConfrontoEventListener.processarConfronto chamado diretamente - delegando para PartidaEncerradaOrchestrator");
        var event = new PartidaEncerradaEvent(partida);
        orchestrator.handlePartidaEncerrada(event);
    }
}
