package br.com.statezone.listeners;

import br.com.statezone.events.PartidaEncerradaEvent;
import br.com.statezone.service.PartidaEncerradaProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartidaEncerradaOrchestrator {

    private final PartidaEncerradaProcessor processor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPartidaEncerrada(PartidaEncerradaEvent event) {
        if (event.getPartida() == null || event.getPartida().getId() == null) {
            return;
        }
        Long partidaId = event.getPartida().getId();
        try {
            processor.processar(partidaId);
        } catch (Exception e) {
            log.error("Erro ao processar partida encerrada {}: {}", partidaId, e.getMessage(), e);
        }
    }
}
