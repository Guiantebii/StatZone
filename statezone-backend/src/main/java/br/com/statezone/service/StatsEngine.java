package br.com.statezone.service;

import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.EscalacaoPartida;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.model.EventoPartida;
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
public class StatsEngine {

    private final EstatisticasJogadorRepository estatisticasJogadorRepository;
    private final EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;
    private final JogadorRepository jogadorRepository;
    private final EscalacaoPartidaRepository escalacaoPartidaRepository;

    public void process(Partida partida) {
        if (partida == null) {
            return;
        }

        Set<Jogador> jogadoresDaPartida = jogadoresDaPartida(partida);

        for (Jogador jogador : jogadoresDaPartida) {
            registrarPartidaJogada(jogador, partida);
        }

        if (partida.getEventos() == null) {
            return;
        }

        for (EventoPartida evento : partida.getEventos()) {
            if (evento == null || evento.getTipoEvento() == null || evento.isAnulado()) {
                continue;
            }

            processarEvento(evento, partida);
        }
    }

    private void processarEvento(EventoPartida evento, Partida partida) {
        Jogador jogador = evento.getJogador();
        if (jogador == null) {
            return;
        }

        switch (evento.getTipoEvento()) {
            case GOL -> {
                registrarGol(jogador, partida);
                registrarAssistencia(evento.getJogadorSecundario(), partida);
            }
            case PENALTI_GOL -> registrarGol(jogador, partida);
            case FINALIZACAO, FINALIZACAO_NO_GOL -> registrarFinalizacao(jogador, partida);
            case FALTA -> registrarFalta(jogador, partida);
            case CARTAO_AMARELO -> registrarCartaoAmarelo(jogador, partida);
            case CARTAO_VERMELHO -> registrarCartaoVermelho(jogador, partida);
            case DEFESA -> registrarDefesa(jogador, partida);
            case PENALTI_DEFENDIDO -> {
                registrarDefesa(jogador, partida);
                registrarPenaltiDefendido(jogador, partida);
            }
            case PENALTI_PERDIDO -> registrarPenaltiPerdido(jogador, partida);
            default -> {
            }
        }
    }

    private Set<Jogador> jogadoresDaPartida(Partida partida) {
        List<Jogador> jogadoresDaEscalacao = escalacaoPartidaRepository
                .findByPartidaIdWithJogador(partida.getId())
                .stream()
                .filter(e -> Boolean.TRUE.equals(e.getAtivo()))
                .map(EscalacaoPartida::getJogador)
                .toList();

        if (!jogadoresDaEscalacao.isEmpty()) {
            return new LinkedHashSet<>(jogadoresDaEscalacao);
        }

        return new LinkedHashSet<>(jogadorRepository.findByTimeIdIn(
                List.of(
                        partida.getTimeMandante().getId(),
                        partida.getTimeVisitante().getId()
                )
        ));
    }

    private void registrarPartidaJogada(Jogador jogador, Partida partida) {
        EstatisticasJogador carreira = estatisticasJogadorRepository
                .findByJogadorId(jogador.getId())
                .orElseGet(() -> {
                    EstatisticasJogador estatisticas = new EstatisticasJogador();
                    estatisticas.setJogador(jogador);
                    return estatisticas;
                });

        EstatisticasJogadorCampeonato campeonato = obterEstatisticasCampeonato(jogador, partida);

        carreira.setPartidasJogadas(somar(carreira.getPartidasJogadas(), 1));
        campeonato.setPartidasJogadas(somar(campeonato.getPartidasJogadas(), 1));

        estatisticasJogadorRepository.save(carreira);
        estatisticasJogadorCampeonatoRepository.save(campeonato);
    }

    private void registrarGol(Jogador jogador, Partida partida) {
        atualizarEstatisticas(jogador, partida, carreira -> carreira.setGols(somar(carreira.getGols(), 1)),
                campeonato -> campeonato.setGols(somar(campeonato.getGols(), 1)));
    }

    private void registrarAssistencia(Jogador jogador, Partida partida) {
        if (jogador == null) {
            return;
        }

        atualizarEstatisticas(jogador, partida,
                carreira -> carreira.setAssistencias(somar(carreira.getAssistencias(), 1)),
                campeonato -> campeonato.setAssistencias(somar(campeonato.getAssistencias(), 1)));
    }

    private void registrarFinalizacao(Jogador jogador, Partida partida) {
        atualizarEstatisticas(jogador, partida,
                carreira -> carreira.setFinalizacoes(somar(carreira.getFinalizacoes(), 1)),
                campeonato -> campeonato.setFinalizacoes(somar(campeonato.getFinalizacoes(), 1)));
    }

    private void registrarFalta(Jogador jogador, Partida partida) {
        atualizarEstatisticas(jogador, partida,
                carreira -> carreira.setFaltasCometidas(somar(carreira.getFaltasCometidas(), 1)),
                campeonato -> campeonato.setFaltasCometidas(somar(campeonato.getFaltasCometidas(), 1)));
    }

    private void registrarCartaoAmarelo(Jogador jogador, Partida partida) {
        atualizarEstatisticas(jogador, partida,
                carreira -> carreira.setCartoesAmarelos(somar(carreira.getCartoesAmarelos(), 1)),
                campeonato -> campeonato.setCartoesAmarelos(somar(campeonato.getCartoesAmarelos(), 1)));
    }

    private void registrarCartaoVermelho(Jogador jogador, Partida partida) {
        atualizarEstatisticas(jogador, partida,
                carreira -> carreira.setCartoesVermelhos(somar(carreira.getCartoesVermelhos(), 1)),
                campeonato -> campeonato.setCartoesVermelhos(somar(campeonato.getCartoesVermelhos(), 1)));
    }

    private void registrarDefesa(Jogador jogador, Partida partida) {
        atualizarEstatisticas(jogador, partida,
                carreira -> carreira.setDefesas(somar(carreira.getDefesas(), 1)),
                campeonato -> campeonato.setDefesas(somar(campeonato.getDefesas(), 1)));
    }

    private void registrarPenaltiDefendido(Jogador jogador, Partida partida) {
        atualizarEstatisticas(jogador, partida,
                carreira -> carreira.setPenaltisDefendidos(somar(carreira.getPenaltisDefendidos(), 1)),
                campeonato -> campeonato.setPenaltisDefendidos(somar(campeonato.getPenaltisDefendidos(), 1)));
    }

    private void registrarPenaltiPerdido(Jogador jogador, Partida partida) {
        atualizarEstatisticas(jogador, partida,
                carreira -> carreira.setPenaltisPerdidos(somar(carreira.getPenaltisPerdidos(), 1)),
                campeonato -> campeonato.setPenaltisPerdidos(somar(campeonato.getPenaltisPerdidos(), 1)));
    }

    private void atualizarEstatisticas(
            Jogador jogador,
            Partida partida,
            java.util.function.Consumer<EstatisticasJogador> atualizarCarreira,
            java.util.function.Consumer<EstatisticasJogadorCampeonato> atualizarCampeonato
    ) {
        if (jogador == null) {
            return;
        }

        EstatisticasJogador carreira = estatisticasJogadorRepository
                .findByJogadorId(jogador.getId())
                .orElseGet(() -> {
                    EstatisticasJogador estatisticas = new EstatisticasJogador();
                    estatisticas.setJogador(jogador);
                    return estatisticas;
                });
        EstatisticasJogadorCampeonato campeonato = obterEstatisticasCampeonato(jogador, partida);

        atualizarCarreira.accept(carreira);
        atualizarCampeonato.accept(campeonato);

        estatisticasJogadorRepository.save(carreira);
        estatisticasJogadorCampeonatoRepository.save(campeonato);
    }

    private EstatisticasJogadorCampeonato obterEstatisticasCampeonato(Jogador jogador, Partida partida) {
        return estatisticasJogadorCampeonatoRepository
                .findByJogadorIdAndCampeonatoId(jogador.getId(), partida.getCampeonato().getId())
                .orElseGet(() -> {
                    EstatisticasJogadorCampeonato estatisticas = new EstatisticasJogadorCampeonato();
                    estatisticas.setJogador(jogador);
                    estatisticas.setCampeonato(partida.getCampeonato());
                    return estatisticas;
                });
    }

    private int somar(Integer valor, int incremento) {
        return (valor == null ? 0 : valor) + incremento;
    }
}
