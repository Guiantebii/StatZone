package br.com.statezone.service;

import br.com.statezone.enums.Posicao;
import br.com.statezone.model.EscalacaoPartida;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.repository.EscalacaoPartidaRepository;
import br.com.statezone.repository.EstatisticasJogadorCampeonatoRepository;
import br.com.statezone.repository.EstatisticasJogadorRepository;
import br.com.statezone.repository.JogadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CleanSheetEngine {

    private final EstatisticasJogadorRepository estatisticasJogadorRepository;
    private final EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;
    private final JogadorRepository jogadorRepository;
    private final EscalacaoPartidaRepository escalacaoPartidaRepository;

    public void process(Partida partida) {
        if (partida == null) {
            return;
        }

        if (partida.getGolsMandante() != null && partida.getGolsMandante() == 0) {
            registrarCleanSheet(partida, partida.getTimeMandante().getId());
        }

        if (partida.getGolsVisitante() != null && partida.getGolsVisitante() == 0) {
            registrarCleanSheet(partida, partida.getTimeVisitante().getId());
        }
    }

    private void registrarCleanSheet(Partida partida, Long timeId) {
        for (Jogador goleiro : goleirosDoTimeNaPartida(partida, timeId)) {
            EstatisticasJogador carreira = estatisticasJogadorRepository
                    .findByJogadorId(goleiro.getId())
                    .orElseGet(() -> {
                        EstatisticasJogador estatisticas = new EstatisticasJogador();
                        estatisticas.setJogador(goleiro);
                        return estatisticas;
                    });

            EstatisticasJogadorCampeonato campeonato = estatisticasJogadorCampeonatoRepository
                    .findByJogadorIdAndCampeonatoId(
                            goleiro.getId(),
                            partida.getCampeonato().getId()
                    )
                    .orElseGet(() -> {
                        EstatisticasJogadorCampeonato estatisticas = new EstatisticasJogadorCampeonato();
                        estatisticas.setJogador(goleiro);
                        estatisticas.setCampeonato(partida.getCampeonato());
                        return estatisticas;
                    });

            carreira.setCleanSheets(somar(carreira.getCleanSheets(), 1));
            campeonato.setCleanSheets(somar(campeonato.getCleanSheets(), 1));

            estatisticasJogadorRepository.save(carreira);
            estatisticasJogadorCampeonatoRepository.save(campeonato);
        }
    }

    private Set<Jogador> goleirosDoTimeNaPartida(Partida partida, Long timeId) {
        List<Jogador> escalados = escalacaoPartidaRepository.findByPartidaIdWithJogador(partida.getId())
                .stream()
                .filter(e -> Boolean.TRUE.equals(e.getAtivo()))
                .filter(e -> e.getJogador() != null)
                .map(EscalacaoPartida::getJogador)
                .filter(j -> j.getTime() != null && timeId.equals(j.getTime().getId()))
                .filter(j -> j.getPosicao() == Posicao.GOLEIRO)
                .toList();

        if (!escalados.isEmpty()) {
            return new LinkedHashSet<>(escalados);
        }

        return new LinkedHashSet<>(jogadorRepository.findByTimeIdAndPosicao(timeId, Posicao.GOLEIRO));
    }

    private int somar(Integer valor, int incremento) {
        return (valor == null ? 0 : valor) + incremento;
    }
}
