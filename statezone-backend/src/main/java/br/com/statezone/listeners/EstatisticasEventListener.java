package br.com.statezone.listeners;

import br.com.statezone.events.PartidaEncerradaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstatisticasEventListener {

    private final PartidaEncerradaOrchestrator orchestrator;

    @EventListener
    public void onEvento(PartidaEncerradaEvent event) {
        log.warn("EstatisticasEventListener chamado diretamente - delegando para PartidaEncerradaOrchestrator");
        orchestrator.handlePartidaEncerrada(event);
    }
}
