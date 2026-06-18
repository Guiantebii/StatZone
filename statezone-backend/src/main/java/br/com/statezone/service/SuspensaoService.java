package br.com.statezone.service;

import br.com.statezone.dto.suspensao.SuspensaoResponseDto;
import br.com.statezone.enums.MotivoSuspensao;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.mapper.SuspensaoMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.Suspensao;
import br.com.statezone.repository.EstatisticasJogadorCampeonatoRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.SuspensaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SuspensaoService {

    private final SuspensaoRepository suspensaoRepository;
    private final PartidaRepository partidaRepository;
    private final SuspensaoMapper suspensaoMapper;
    private final EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;

    public List<SuspensaoResponseDto> listarSuspensoesPorRodada(
            Long campeonatoId,
            Integer rodada
    ) {
        return suspensaoRepository
                .findByCampeonatoIdAndRodadaSuspensao(campeonatoId, rodada)
                .stream()
                .map(suspensaoMapper::toDto)
                .toList();
    }

    public List<SuspensaoResponseDto> listarSuspensoesProximaRodada(Long campeonatoId) {
        Integer proximaRodada = partidaRepository
                .findProximaRodada(campeonatoId);

        if (proximaRodada == null) {
            return List.of();
        }

        return listarSuspensoesPorRodada(campeonatoId, proximaRodada);
    }

    public void registrarEventoDisciplinar(
            Jogador jogador,
            Partida partidaAtual,
            TipoEvento tipoEvento
    ) {
        if (jogador == null || partidaAtual == null || tipoEvento == null) {
            return;
        }

        switch (tipoEvento) {
            case CARTAO_AMARELO -> registrarAcumuloDeAmarelos(jogador, partidaAtual);
            case CARTAO_VERMELHO -> suspenderSeNecessario(
                    jogador,
                    partidaAtual,
                    MotivoSuspensao.CARTAO_VERMELHO
            );
            default -> {
            }
        }
    }

    private void registrarAcumuloDeAmarelos(Jogador jogador, Partida partidaAtual) {
        EstatisticasJogadorCampeonato estatisticas = obterEstatisticasDoCampeonato(jogador, partidaAtual);
        Campeonato campeonato = partidaAtual.getCampeonato();
        int limite = Optional.ofNullable(campeonato.getAmarelosParaSuspensao())
                .filter(valor -> valor > 0)
                .orElse(3);

        int totalAtual = valorOuZero(estatisticas.getAmarelosDesdeSuspensao()) + 1;
        estatisticas.setAmarelosDesdeSuspensao(totalAtual);
        estatisticasJogadorCampeonatoRepository.save(estatisticas);

        if (totalAtual >= limite) {
            estatisticas.setAmarelosDesdeSuspensao(0);
            estatisticasJogadorCampeonatoRepository.save(estatisticas);
            suspenderSeNecessario(jogador, partidaAtual, MotivoSuspensao.ACUMULO_AMARELOS);
        }
    }

    public void suspenderSeNecessario(Jogador jogador, Partida partidaAtual, MotivoSuspensao motivo) {
        Long campeonatoId = partidaAtual.getCampeonato().getId();
        Long timeId = jogador.getTime().getId();

        List<Partida> proximas = partidaRepository
                .findProximasPartidasDoTime(campeonatoId, timeId, PageRequest.of(0, 1));
        Partida proximaPartida = proximas.isEmpty() ? null : proximas.get(0);

        boolean jaSuspenso = proximaPartida != null
                ? suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoId(
                jogador.getId(), campeonatoId, proximaPartida.getId())
                : suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoIsNull(
                jogador.getId(), campeonatoId);

        if (jaSuspenso) return;

        Suspensao suspensao = new Suspensao();
        suspensao.setJogador(jogador);
        suspensao.setCampeonato(partidaAtual.getCampeonato());
        suspensao.setMotivo(motivo);
        suspensao.setPartidaAlvo(proximaPartida);
        suspensao.setRodadaSuspensao(
                proximaPartida != null
                        ? proximaPartida.getRodada()
                        : null
        );

        suspensaoRepository.save(suspensao);
    }

    private EstatisticasJogadorCampeonato obterEstatisticasDoCampeonato(
            Jogador jogador,
            Partida partidaAtual
    ) {
        return estatisticasJogadorCampeonatoRepository
                .findByJogadorIdAndCampeonatoId(
                        jogador.getId(),
                        partidaAtual.getCampeonato().getId()
                )
                .orElseGet(() -> {
                    EstatisticasJogadorCampeonato estatisticas = new EstatisticasJogadorCampeonato();
                    estatisticas.setJogador(jogador);
                    estatisticas.setCampeonato(partidaAtual.getCampeonato());
                    return estatisticas;
                });
    }

    private int valorOuZero(Integer valor) {
        return valor == null ? 0 : valor;
    }
}
