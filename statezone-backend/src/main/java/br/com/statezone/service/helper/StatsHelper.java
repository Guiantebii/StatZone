package br.com.statezone.service.helper;

import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.repository.EstatisticasJogadorCampeonatoRepository;
import br.com.statezone.repository.EstatisticasJogadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatsHelper {

    private final EstatisticasJogadorRepository estatisticasJogadorRepository;
    private final EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;

    public EstatisticasJogador buscarOuCriarCarreira(Jogador jogador) {
        return estatisticasJogadorRepository
                .findByJogadorId(jogador.getId())
                .orElseGet(() -> {
                    EstatisticasJogador e = new EstatisticasJogador();
                    e.setJogador(jogador);
                    return e;
                });
    }

    public EstatisticasJogadorCampeonato obterOuCriarCampeonato(Jogador jogador, Partida partida) {
        return estatisticasJogadorCampeonatoRepository
                .findByJogadorIdAndCampeonatoId(jogador.getId(), partida.getCampeonato().getId())
                .orElseGet(() -> {
                    EstatisticasJogadorCampeonato e = new EstatisticasJogadorCampeonato();
                    e.setJogador(jogador);
                    e.setCampeonato(partida.getCampeonato());
                    return e;
                });
    }

    public void salvarAmbos(EstatisticasJogador carreira, EstatisticasJogadorCampeonato campeonato) {
        estatisticasJogadorRepository.save(carreira);
        estatisticasJogadorCampeonatoRepository.save(campeonato);
    }
}