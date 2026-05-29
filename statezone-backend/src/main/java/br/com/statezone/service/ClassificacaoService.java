package br.com.statezone.service;

import br.com.statezone.dto.classificacao.ClassificacaoResponseDto;
import br.com.statezone.mapper.ClassificacaoMapper;
import br.com.statezone.service.ranking.RankingCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class ClassificacaoService {
        private final RankingCacheService rankingCacheService;
        private final ClassificacaoMapper classificacaoMapper;

        public List<ClassificacaoResponseDto> gerarClassificacao(Long campeonatoId) {
            return rankingCacheService.getRanking(campeonatoId)
                    .stream()
                    .map(classificacaoMapper::toDto)
                    .toList();
        }
}

