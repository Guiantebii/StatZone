package br.com.statezone.service;

import br.com.statezone.dto.classificacao.ClassificacaoResponseDto;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.ClassificacaoMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Grupo;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.GrupoRepository;
import br.com.statezone.service.helper.ClassificacaoStats;
import br.com.statezone.service.ranking.RankingCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Transactional
public class ClassificacaoService {
        private final RankingCacheService rankingCacheService;
        private final ClassificacaoMapper classificacaoMapper;
        private final GrupoRepository grupoRepository;
        private final CampeonatoRepository campeonatoRepository;

        public List<ClassificacaoResponseDto> gerarClassificacao(Long campeonatoId) {
            Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));

            Map<Long, ClassificacaoStats> statsMap = rankingCacheService.getRanking(campeonatoId)
                    .stream()
                    .collect(Collectors.toMap(ClassificacaoStats::getTimeId, s -> s));

            List<ClassificacaoStats> completo = campeonato.getTimes().stream()
                    .map(time -> statsMap.containsKey(time.getId())
                            ? statsMap.get(time.getId())
                            : new ClassificacaoStats(time))
                    .sorted((a, b) -> {
                        int cmp = b.getPontos().compareTo(a.getPontos());
                        if (cmp != 0) return cmp;
                        cmp = b.getSaldoGols().compareTo(a.getSaldoGols());
                        if (cmp != 0) return cmp;
                        return b.getGolsFeitos().compareTo(a.getGolsFeitos());
                    })
                    .toList();

            for (int i = 0; i < completo.size(); i++) {
                ClassificacaoStats s = completo.get(i);
                s.setPosicao(i + 1);
                double ap = s.getJogos() > 0
                        ? Math.round((s.getPontos() / (double)(s.getJogos() * 3)) * 1000.0) / 10.0
                        : 0.0;
                s.setAproveitamento(ap);
            }

            return completo.stream()
                    .map(classificacaoMapper::toDto)
                    .toList();
        }

    public List<ClassificacaoResponseDto> gerarClassificacaoPorGrupo(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        Map<Long, ClassificacaoStats> statsMap = rankingCacheService.getRankingPorGrupo(grupoId)
                .stream()
                .collect(Collectors.toMap(ClassificacaoStats::getTimeId, s -> s));

        List<ClassificacaoStats> completo = grupo.getTimes().stream()
                .map(time -> statsMap.containsKey(time.getId())
                        ? statsMap.get(time.getId())
                        : new ClassificacaoStats(time))
                .sorted((a, b) -> {
                    int cmp = b.getPontos().compareTo(a.getPontos());
                    if (cmp != 0) return cmp;
                    cmp = b.getSaldoGols().compareTo(a.getSaldoGols());
                    if (cmp != 0) return cmp;
                    return b.getGolsFeitos().compareTo(a.getGolsFeitos());
                })
                .toList();

        for (int i = 0; i < completo.size(); i++) {
            ClassificacaoStats s = completo.get(i);
            s.setPosicao(i + 1);
            double ap = s.getJogos() > 0
                    ? Math.round((s.getPontos() / (double)(s.getJogos() * 3)) * 1000.0) / 10.0
                    : 0.0;
            s.setAproveitamento(ap);
        }

        return completo.stream()
                .map(classificacaoMapper::toDto)
                .toList();
    }

    public List<ClassificacaoResponseDto> gerarClassificacaoPorTurno(
            Long campeonatoId,
            Integer turno
    ) {
        if (turno != 1 && turno != 2) {
            throw new BusinessException("Turno inválido — informe 1 ou 2");
        }

        return rankingCacheService.getRankingPorTurno(campeonatoId, turno)
                .stream()
                .map(classificacaoMapper::toDto)
                .toList();
    }
}

