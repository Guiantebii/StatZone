package br.com.statezone.service;

import br.com.statezone.dto.ArtilhariaResponseDto;
import br.com.statezone.dto.EstatisticasJogadorResponseDto;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.EstatisticasJogadorMapper;
import br.com.statezone.repository.EstatisticasJogadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class EstatisticasJogadorService {

    private final EstatisticasJogadorRepository estatisticasJogadorRepository;
    private final EstatisticasJogadorMapper estatisticasJogadorMapper;

    public EstatisticasJogadorResponseDto buscarPorJogador(Long jogadorId){
        return estatisticasJogadorRepository.findByJogadorId(jogadorId)
                .map(estatisticasJogadorMapper::toDto)
                .orElseThrow(()-> new ResourceNotFoundException ("Estatisticas não encontradas para esse jogador"));
    }

    public List<ArtilhariaResponseDto> artilharia(Long campeonatoId){
        var lista = estatisticasJogadorRepository.findArtilhariasByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> {
                    ArtilhariaResponseDto dto = estatisticasJogadorMapper.toArtilhariaDto(e);
                    return  new ArtilhariaResponseDto(
                            posicao.getAndIncrement(),
                            dto.jogadorId(),
                            dto.nomeJogador(),
                            dto.nomeTime(),
                            dto.escudoTime(),
                            dto.gols()
                    );
                })
                .toList();
    }
}
