package br.com.statezone.listeners;

import br.com.statezone.events.EventoPartidaCriadaEvent;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasPartida;
import br.com.statezone.repository.EstatisticasJogadorRepository;
import br.com.statezone.repository.EstatisticasPartidaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstatisticasEventListener {

    private final EstatisticasPartidaRepository repository;
    private final EstatisticasJogadorRepository estatisticasJogadorRepository;

    @EventListener
    @Transactional
    public void onEvento(EventoPartidaCriadaEvent event) {

        var e = event.evento();
        var partida = event.partida();

        boolean mandante =
                e.getTime().getId().equals(partida.getTimeMandante().getId());

        EstatisticasPartida stats = repository.findByPartidaId(partida.getId())
                .orElseGet(() -> {
                    EstatisticasPartida s = new EstatisticasPartida();
                    s.setPartida(partida);
                    return repository.saveAndFlush(s);
                });

        EstatisticasJogador statsJogador = null;

        if (e.getJogador() != null) {

            statsJogador = estatisticasJogadorRepository
                    .findByJogadorId(e.getJogador().getId())
                    .orElseGet(() -> {

                        EstatisticasJogador s =
                                new EstatisticasJogador();

                        s.setJogador(e.getJogador());

                        return estatisticasJogadorRepository
                                .saveAndFlush(s);
                    });
        }

        switch (e.getTipoEvento()) {

            case FINALIZACAO, FINALIZACAO_NO_GOL -> {
                if (mandante) {
                    stats.setFinalizacoesMandante(
                            stats.getFinalizacoesMandante() + 1
                    );
                } else {
                    stats.setFinalizacoesVisitante(
                            stats.getFinalizacoesVisitante() + 1
                    );
                }

                statsJogador.setFinalizacoes(
                        statsJogador.getFinalizacoes() + 1
                );
            }

            case GOL, PENALTI_GOL -> {

                if (mandante) {
                    stats.setFinalizacoesMandante(
                            stats.getFinalizacoesMandante() + 1
                    );

                    stats.setFinalizacoesGolMandante(
                            stats.getFinalizacoesGolMandante() + 1
                    );

                } else {

                    stats.setFinalizacoesVisitante(
                            stats.getFinalizacoesVisitante() + 1
                    );

                    stats.setFinalizacoesGolVisitante(
                            stats.getFinalizacoesGolVisitante() + 1
                    );
                }

                statsJogador.setFinalizacoes(
                        statsJogador.getFinalizacoes() + 1
                );

                statsJogador.setGols(
                        statsJogador.getGols() + 1
                );

                if (e.getJogadorSecundario() != null) {

                    EstatisticasJogador assistStats =
                            estatisticasJogadorRepository
                                    .findByJogadorId(
                                            e.getJogadorSecundario().getId()
                                    )
                                    .orElseGet(() -> {

                                        EstatisticasJogador s =
                                                new EstatisticasJogador();

                                        s.setJogador(
                                                e.getJogadorSecundario()
                                        );

                                        return estatisticasJogadorRepository
                                                .saveAndFlush(s);
                                    });

                    assistStats.setAssistencias(
                            assistStats.getAssistencias() + 1
                    );

                    estatisticasJogadorRepository.save(assistStats);
                }
            }

            case GOL_CONTRA -> {

                if (mandante) {
                    stats.setFinalizacoesGolVisitante(
                            stats.getFinalizacoesGolVisitante() + 1
                    );
                } else {
                    stats.setFinalizacoesGolMandante(
                            stats.getFinalizacoesGolMandante() + 1
                    );
                }
            }

            case FALTA -> {

                if (mandante) {
                    stats.setFaltasMandante(
                            stats.getFaltasMandante() + 1
                    );
                } else {
                    stats.setFaltasVisitante(
                            stats.getFaltasVisitante() + 1
                    );
                }

                statsJogador.setFaltasCometidas(
                        statsJogador.getFaltasCometidas() + 1
                );
            }

            case CARTAO_AMARELO -> {

                if (mandante) {
                    stats.setCartoesAmarelosMandante(
                            stats.getCartoesAmarelosMandante() + 1
                    );
                } else {
                    stats.setCartoesAmarelosVisitante(
                            stats.getCartoesAmarelosVisitante() + 1
                    );
                }

                statsJogador.setCartoesAmarelos(
                        statsJogador.getCartoesAmarelos() + 1
                );
            }

            case CARTAO_VERMELHO -> {

                if (mandante) {
                    stats.setCartoesVermelhosMandante(
                            stats.getCartoesVermelhosMandante() + 1
                    );
                } else {
                    stats.setCartoesVermelhosVisitante(
                            stats.getCartoesVermelhosVisitante() + 1
                    );
                }

                statsJogador.setCartoesVermelhos(
                        statsJogador.getCartoesVermelhos() + 1
                );
            }

            case ESCANTEIO -> {

                if (mandante) {
                    stats.setEscanteiosMandante(
                            stats.getEscanteiosMandante() + 1
                    );
                } else {
                    stats.setEscanteiosVisitante(
                            stats.getEscanteiosVisitante() + 1
                    );
                }
            }

            case DEFESA -> {

                if (mandante) {
                    stats.setDefesasMandante(
                            stats.getDefesasMandante() + 1
                    );
                } else {
                    stats.setDefesasVisitante(
                            stats.getDefesasVisitante() + 1
                    );
                }

                statsJogador.setDefesas(
                        statsJogador.getDefesas() + 1
                );
            }

            case PENALTI_DEFENDIDO -> {

                if (mandante) {
                    stats.setPenaltisDefendidosMandante(
                            stats.getPenaltisDefendidosMandante() + 1
                    );
                } else {
                    stats.setPenaltisDefendidosVisitante(
                            stats.getPenaltisDefendidosVisitante() + 1
                    );
                }

                statsJogador.setPenaltisDefendidos(
                        statsJogador.getPenaltisDefendidos() + 1
                );
            }

            case PENALTI_PERDIDO -> {
                statsJogador.setPenaltisPerdidos(
                        statsJogador.getPenaltisPerdidos() + 1
                );
            }

            case IMPEDIMENTO -> {
                // implementar depois
            }

            case SUBSTITUICAO -> {
                // implementar depois
            }
            case VAR_GOL_CONFIRMADO,
                 VAR_GOL_ANULADO,
                 INICIO_PRIMEIRO_TEMPO,
                 FIM_PRIMEIRO_TEMPO,
                 INICIO_SEGUNDO_TEMPO,
                 FIM_PARTIDA -> {
                // não alteram estatísticas
            }
        }

        repository.save(stats);

        if (statsJogador != null) {
            estatisticasJogadorRepository.save(statsJogador);
        }
    }
}