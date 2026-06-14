package br.com.statezone.service;

import br.com.statezone.dto.rankings.*;
import br.com.statezone.dto.estatisticasJogador.EstatisticasJogadorResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.EstatisticasJogadorMapper;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.repository.EstatisticasJogadorCampeonatoRepository;
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
    private final EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;
    private final EstatisticasJogadorMapper estatisticasJogadorMapper;
    private final PartidaRepository partidaRepository;

    public EstatisticasJogadorResponseDto buscarPorJogador(Long jogadorId) {
        return estatisticasJogadorRepository.findByJogadorId(jogadorId)
                .map(estatisticasJogadorMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estatisticas não encontradas para esse jogador"));
    }

    public List<ArtilhariaResponseDto> artilharia(Long campeonatoId) {
        var lista = estatisticasJogadorCampeonatoRepository
                .findArtilheirosByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> new ArtilhariaResponseDto(
                        posicao.getAndIncrement(),
                        e.getJogador().getId(),
                        e.getJogador().getNome(),
                        e.getJogador().getTime().getNome(),
                        e.getJogador().getTime().getEscudoUrl(),
                        e.getGols()
                ))
                .toList();
    }

    public List<AssistenciaRankingResponseDto> rankingAssistencias(Long campeonatoId) {
        var lista = estatisticasJogadorCampeonatoRepository
                .findAssistentesByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> new AssistenciaRankingResponseDto(
                        posicao.getAndIncrement(),
                        e.getJogador().getId(),
                        e.getJogador().getNome(),
                        e.getJogador().getTime().getNome(),
                        e.getJogador().getTime().getEscudoUrl(),
                        e.getAssistencias()
                ))
                .toList();
    }

    public List<RankingCartaoAmareloResponseDto> rankingCartaoAmarelo(Long campeonatoId) {
        var lista = estatisticasJogadorCampeonatoRepository
                .findCartoesAmarelosByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> new RankingCartaoAmareloResponseDto(
                        posicao.getAndIncrement(),
                        e.getJogador().getId(),
                        e.getJogador().getNome(),
                        e.getJogador().getTime().getNome(),
                        e.getJogador().getTime().getEscudoUrl(),
                        e.getCartoesAmarelos()
                ))
                .toList();
    }

    public List<RankingCartaoVermelhoResponseDto> rankingCartaoVermelho(Long campeonatoId) {
        var lista = estatisticasJogadorCampeonatoRepository
                .findCartoesVermelhosByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> new RankingCartaoVermelhoResponseDto(
                        posicao.getAndIncrement(),
                        e.getJogador().getId(),
                        e.getJogador().getNome(),
                        e.getJogador().getTime().getNome(),
                        e.getJogador().getTime().getEscudoUrl(),
                        e.getCartoesVermelhos()
                ))
                .toList();
    }

    public List<SelecaoCampeonatoResponseDto> gerarSelecaoDoCampeonato(Long campeonatoId) {
        long totalPartidasEncerradas = partidaRepository
                .countByCampeonatoIdAndStatus(campeonatoId, StatusPartida.ENCERRADA);

        int minPartidas = (totalPartidasEncerradas > 0)
                ? (int) (totalPartidasEncerradas / 2)
                : 1;

        List<EstatisticasJogadorCampeonato> estatisticas =
                estatisticasJogadorCampeonatoRepository
                        .findParaDestaques(campeonatoId, minPartidas);

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

    public CraqueCampeonatoResponseDto mvpCampeonato(Long campeonatoId) {
        long totalPartidasEncerradas = partidaRepository
                .countByCampeonatoIdAndStatus(campeonatoId, StatusPartida.ENCERRADA);

        int minPartidas = (totalPartidasEncerradas > 0)
                ? (int) (totalPartidasEncerradas / 2)
                : 1;

        List<EstatisticasJogadorCampeonato> estatisticas =
                estatisticasJogadorCampeonatoRepository
                        .findParaDestaques(campeonatoId, minPartidas);

        EstatisticasJogadorCampeonato mvp = estatisticas.stream()
                .max(Comparator.comparingDouble(this::calcularScore))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhum jogador encontrado"));

        return estatisticasJogadorMapper.toCraqueCampeonatoDto(mvp, calcularScore(mvp));
    }

    public List<RankingGoleiroResponseDto> rankingGoleiros(Long campeonatoId) {
        var lista = estatisticasJogadorCampeonatoRepository
                .findRankingGoleirosByCampeonatoId(campeonatoId);

        AtomicInteger posicao = new AtomicInteger(1);

        return lista.stream()
                .map(e -> new RankingGoleiroResponseDto(
                        posicao.getAndIncrement(),
                        e.getJogador().getId(),
                        e.getJogador().getNome(),
                        e.getJogador().getFotoUrl(),
                        e.getJogador().getTime().getNome(),
                        e.getJogador().getTime().getEscudoUrl(),
                        e.getCleanSheets(),
                        e.getDefesas(),
                        e.getPenaltisDefendidos(),
                        e.getPartidasJogadas()
                ))
                .toList();
    }

    private double calcularScore(EstatisticasJogadorCampeonato e) {
        return (e.getGols() * 5.0)
                + (e.getAssistencias() * 3.0)
                + (e.getDefesas() * 1.5)
                + (e.getPenaltisDefendidos() * 4.0)
                - (e.getPenaltisPerdidos() * 1.0)
                - (e.getCartoesAmarelos() * 0.5)
                - (e.getCartoesVermelhos() * 2.0);
    }
}