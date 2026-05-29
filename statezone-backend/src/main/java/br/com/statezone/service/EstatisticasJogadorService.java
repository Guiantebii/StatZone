package br.com.statezone.service;

import br.com.statezone.dto.rankings.ArtilhariaResponseDto;
import br.com.statezone.dto.rankings.AssistenciaRankingResponseDto;
import br.com.statezone.dto.estatisticasJogador.EstatisticasJogadorResponseDto;
import br.com.statezone.dto.rankings.RankingCartaoAmareloResponseDto;
import br.com.statezone.dto.rankings.RankingCartaoVermelhoResponseDto;
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
        var lista = estatisticasJogadorRepository.findArtilheirosByCampeonatoId(campeonatoId);

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

    public List<AssistenciaRankingResponseDto> rankingAssistencias(Long campeonatoId){
        var lista = estatisticasJogadorRepository.findAssistentesByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> {
                    AssistenciaRankingResponseDto dto = estatisticasJogadorMapper.toAssistenciaDto(e);
                    return new AssistenciaRankingResponseDto(
                            posicao.getAndIncrement(),
                            dto.jogadorId(),
                            dto.nomeJogador(),
                            dto.nomeTime(),
                            dto.escudoTime(),
                            dto.assistencias()
                    );
                })
                .toList();
    }
    public List<RankingCartaoAmareloResponseDto> rankingCartaoAmarelo(Long campeonatoId){
        var lista = estatisticasJogadorRepository.findCartoesAmarelosByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> {
                    RankingCartaoAmareloResponseDto dto = estatisticasJogadorMapper.toCartaoAmareloDto(e);
                    return new RankingCartaoAmareloResponseDto(
                            posicao.getAndIncrement(),
                            dto.jogadorId(),
                            dto.nomeJogador(),
                            dto.nomeTime(),
                            dto.escudoTime(),
                            dto.cartoesAmarelos()
                    );
                })
                .toList();
    }
    public List<RankingCartaoVermelhoResponseDto> rankingCartaoVermelho(Long campeonatoId){
        var lista = estatisticasJogadorRepository.findCartoesVermelhosByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> {
                    RankingCartaoVermelhoResponseDto dto = estatisticasJogadorMapper.toCartaoVermelhoDto(e);
                    return new RankingCartaoVermelhoResponseDto(
                            posicao.getAndIncrement(),
                            dto.jogadorId(),
                            dto.nomeJogador(),
                            dto.nomeTime(),
                            dto.escudoTime(),
                            dto.cartoesVermelhos()
                    );
                })
                .toList();
    }



}
