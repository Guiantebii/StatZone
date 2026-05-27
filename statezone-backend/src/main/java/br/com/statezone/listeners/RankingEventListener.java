package br.com.statezone.listeners;

import br.com.statezone.events.RankingAtualizadoEvent;
import br.com.statezone.service.ranking.RankingCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingEventListener {

    private final RankingCacheService rankingCacheService;

    @EventListener
    public void onRankingUpdate(RankingAtualizadoEvent event) {

        Long campeonatoId = event.partida()
                .getCampeonato()
                .getId();

        rankingCacheService.recalcular(campeonatoId);
    }
}