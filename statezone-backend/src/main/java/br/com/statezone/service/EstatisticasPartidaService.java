package br.com.statezone.service;

import br.com.statezone.dto.EstatisticasPartidaResponseDto;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.EstatisticasPartidaMapper;
import br.com.statezone.model.EstatisticasPartida;
import br.com.statezone.repository.EstatisticasPartidaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EstatisticasPartidaService {

    private final EstatisticasPartidaRepository estatisticasPartidaRepository;
    private final EstatisticasPartidaMapper mapper;

    public EstatisticasPartidaResponseDto gerar(Long partidaId) {

        EstatisticasPartida stats = estatisticasPartidaRepository
                .findByPartidaId(partidaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Estatísticas ainda não existem para essa partida")
                );

        return mapper.toDto(stats);
    }
}