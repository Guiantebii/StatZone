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

    private final ConcurrentHashMap<String, List<ClassificacaoStats>> cache =
            new ConcurrentHashMap<>();

    public List<ClassificacaoStats> getRanking(Long campeonatoId) {
        String key = campeonatoId + "_geral";
        return cache.computeIfAbsent(key, k -> rankingEngine.gerar(campeonatoId));
    }

    public List<ClassificacaoStats> getRankingPorTurno(Long campeonatoId, Integer turno) {
        String key = campeonatoId + "_turno_" + turno;
        return cache.computeIfAbsent(key, k -> rankingEngine.gerarPorTurno(campeonatoId, turno));
    }

    public List<ClassificacaoStats> getRankingPorGrupo(Long grupoId) {
        String key = grupoId + "_grupo";
        return cache.computeIfAbsent(key, k -> rankingEngine.gerarPorGrupo(grupoId));
    }

    public void recalcular(Long campeonatoId) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(campeonatoId + "_"));
        cache.put(campeonatoId + "_geral", rankingEngine.gerar(campeonatoId));
    }
}