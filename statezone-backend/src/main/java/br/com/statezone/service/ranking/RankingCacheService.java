package br.com.statezone.service.ranking;

import br.com.statezone.service.helper.ClassificacaoStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RankingCacheService {

    private static final int MAX_CACHE_SIZE = 200;
    private static final long TTL_SECONDS = 60;

    private final RankingEngine rankingEngine;

    private final ConcurrentHashMap<String, CacheEntry> cache =
            new ConcurrentHashMap<>();

    public List<ClassificacaoStats> getRanking(Long campeonatoId) {
        evictIfNeeded();
        String key = campeonatoId + "_geral";
        return getOrCompute(key, k -> rankingEngine.gerar(campeonatoId));
    }

    public List<ClassificacaoStats> getRankingPorTurno(Long campeonatoId, Integer turno) {
        evictIfNeeded();
        String key = campeonatoId + "_turno_" + turno;
        return getOrCompute(key, k -> rankingEngine.gerarPorTurno(campeonatoId, turno));
    }

    public List<ClassificacaoStats> getRankingPorGrupo(Long grupoId) {
        evictIfNeeded();
        String key = grupoId + "_grupo";
        return getOrCompute(key, k -> rankingEngine.gerarPorGrupo(grupoId));
    }

    private List<ClassificacaoStats> getOrCompute(String key, java.util.function.Function<String, List<ClassificacaoStats>> loader) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.value();
        }
        List<ClassificacaoStats> value = loader.apply(key);
        cache.put(key, new CacheEntry(value, Instant.now().plusSeconds(TTL_SECONDS)));
        return value;
    }

    private void evictIfNeeded() {
        if (cache.size() >= MAX_CACHE_SIZE) {
            cache.clear();
        }
    }

    public void recalcular(Long campeonatoId) {
        cache.keySet().removeIf(key -> key.startsWith(campeonatoId + "_"));
        String key = campeonatoId + "_geral";
        cache.put(key, new CacheEntry(rankingEngine.gerar(campeonatoId), Instant.now().plusSeconds(TTL_SECONDS)));
    }

    private record CacheEntry(List<ClassificacaoStats> value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}