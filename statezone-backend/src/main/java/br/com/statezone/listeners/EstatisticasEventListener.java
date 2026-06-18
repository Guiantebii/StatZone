package br.com.statezone.listeners;

import br.com.statezone.events.PartidaEncerradaEvent;
import br.com.statezone.service.MatchEngine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstatisticasEventListener {

    private final MatchEngine matchEngineService;

    @EventListener
    @Transactional
    public void onEvento(PartidaEncerradaEvent event) {
        matchEngineService.process(event.getPartida());
    }
}
