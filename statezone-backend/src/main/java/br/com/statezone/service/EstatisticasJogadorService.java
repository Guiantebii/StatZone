package br.com.statezone.service;

import br.com.statezone.dto.rankings.*;
import br.com.statezone.dto.estatisticasJogador.EstatisticasJogadorResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.EstatisticasJogadorMapper;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.repository.EstatisticasJogadorRepository;
import br.com.statezone.repository.PartidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstatisticasJogadorService {

    private final EstatisticasJogadorRepository estatisticasJogadorRepository;
    private final EstatisticasJogadorMapper estatisticasJogadorMapper;
    private final PartidaRepository partidaRepository;

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

    public List<SelecaoCampeonatoResponseDto> gerarSelecaoDoCampeonato(Long campeonatoId) {
        long totalPartidasEncerradas = partidaRepository.countByCampeonatoIdAndStatus(campeonatoId, StatusPartida.ENCERRADA);
        int minPartidas = (totalPartidasEncerradas > 0) ? (int)(totalPartidasEncerradas / 2) : 1;

        List<EstatisticasJogador> estatisticas = estatisticasJogadorRepository.findParaDestaques(campeonatoId, minPartidas);

        return estatisticas.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getJogador().getPosicao(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(this::calcularScore)),
                                opt -> opt.map(e -> new SelecaoCampeonatoResponseDto(
                                        e.getJogador().getPosicao().name(),
                                        e.getJogador().getNome(),
                                        e.getJogador().getTime().getNome(),
                                        calcularScore(e)
                                )).orElse(null)
                        )
                ))
                .values().stream()
                .filter(Objects::nonNull)
                .toList();
    }

    public CraqueCampeonatoResponseDto mvpCampeonato(Long campeonatoId){
        long totalPartidasEncerradas = partidaRepository
                .countByCampeonatoIdAndStatus(campeonatoId, StatusPartida.ENCERRADA);

        int minPartidas = (totalPartidasEncerradas > 0)
                ? (int) (totalPartidasEncerradas /2)
                :1;
        List<EstatisticasJogador> estatisticas =
                estatisticasJogadorRepository.findParaDestaques(campeonatoId,minPartidas);

        EstatisticasJogador mvp = estatisticas.stream()
                .max(Comparator.comparingDouble(this::calcularScore))
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum jogador encontrado"));

        double score = calcularScore(mvp);

        return estatisticasJogadorMapper.toCraqueCampeonatoDto(
                mvp,
                score
        );
    }


    private double calcularScore(EstatisticasJogador e) {
        return (e.getGols() * 5.0)
                + (e.getAssistencias() * 3.0)
                + (e.getDefesas() * 1.5)
                + (e.getPenaltisDefendidos() * 4.0)
                - (e.getPenaltisPerdidos() * 1.0)
                - (e.getCartoesAmarelos() * 0.5)
                - (e.getCartoesVermelhos() * 2.0);
    }

}
