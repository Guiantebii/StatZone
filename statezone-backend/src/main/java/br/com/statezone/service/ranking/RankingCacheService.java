package br.com.statezone.service.ranking;

import br.com.statezone.service.helper.ClassificacaoStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RankingCacheService {

    private final RankingEngine rankingEngine;

    private final ConcurrentHashMap<Long, List<ClassificacaoStats>> cache =
            new ConcurrentHashMap<>();

    public List<ClassificacaoStats> getRanking(Long campeonatoId) {

        return cache.computeIfAbsent(
                campeonatoId,
                rankingEngine::gerar
        );
    }

    public void recalcular(Long campeonatoId) {

        List<ClassificacaoStats> novoRanking =
                rankingEngine.gerar(campeonatoId);

        cache.put(campeonatoId, novoRanking);
    }
}