package br.com.statezone.service;

import br.com.statezone.dto.ClassificacaoResponseDto;
import br.com.statezone.mapper.ClassificacaoMapper;
import br.com.statezone.service.ranking.RankingEngine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class ClassificacaoService {
    private final RankingEngine rankingEngine;
    private final ClassificacaoMapper classificacaoMapper;

    public List<ClassificacaoResponseDto> gerarClassificacao(Long campeonatoId) {

        return rankingEngine.gerar(campeonatoId)
                .stream()
                .map(classificacaoMapper::toDto)
                .toList();
    }
}
