package br.com.statezone.service;

import br.com.statezone.model.*;
import br.com.statezone.service.ranking.RankingCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MatchEngine {

    private final StatsEngine statsEngine;
    private final CleanSheetEngine cleanSheetEngine;
    private final SuspensionEngine suspensionEngine;
    private final RankingCacheService rankingCacheService;

    public void process(Partida partida) {

        statsEngine.process(partida);

        cleanSheetEngine.process(partida);

        suspensionEngine.process(partida);

        rankingCacheService.recalcular(partida.getCampeonato().getId());
    }
}