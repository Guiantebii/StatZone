package br.com.statezone.listeners;

import br.com.statezone.enums.MotivoSuspensao;
import br.com.statezone.events.EventoPartidaCriadaEvent;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.EstatisticasPartida;
import br.com.statezone.model.Suspensao;
import br.com.statezone.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstatisticasEventListener {

    private final EstatisticasPartidaRepository repository;
    private final EstatisticasJogadorRepository estatisticasJogadorRepository;
    private final EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;
    private final EscalacaoPartidaRepository escalacaoPartidaRepository;
    private final SuspensaoRepository suspensaoRepository;

    @EventListener
    @Transactional
    public void onEvento(EventoPartidaCriadaEvent event) {

        var e = event.evento();
        var partida = event.partida();

        if (e.getTime() == null) return;

        boolean mandante =
                e.getTime().getId().equals(partida.getTimeMandante().getId());

        EstatisticasPartida stats = repository.findByPartidaId(partida.getId())
                .orElseGet(() -> {
                    EstatisticasPartida s = new EstatisticasPartida();
                    s.setPartida(partida);
                    return repository.saveAndFlush(s);
                });

        EstatisticasJogador statsJogadorCarreira = null;
        EstatisticasJogadorCampeonato statsJogadorCampeonato = null;

        if (e.getJogador() != null) {

            // carreira
            statsJogadorCarreira = estatisticasJogadorRepository
                    .findByJogadorId(e.getJogador().getId())
                    .orElseGet(() -> {
                        EstatisticasJogador s = new EstatisticasJogador();
                        s.setJogador(e.getJogador());
                        return estatisticasJogadorRepository.saveAndFlush(s);
                    });

            // campeonato
            statsJogadorCampeonato = estatisticasJogadorCampeonatoRepository
                    .findByJogadorIdAndCampeonatoId(
                            e.getJogador().getId(),
                            partida.getCampeonato().getId()
                    )
                    .orElseGet(() -> {
                        EstatisticasJogadorCampeonato s = new EstatisticasJogadorCampeonato();
                        s.setJogador(e.getJogador());
                        s.setCampeonato(partida.getCampeonato());
                        return estatisticasJogadorCampeonatoRepository.saveAndFlush(s);
                    });
        }

        switch (e.getTipoEvento()) {

            case FINALIZACAO, FINALIZACAO_NO_GOL -> {
                if (mandante) {
                    stats.setFinalizacoesMandante(stats.getFinalizacoesMandante() + 1);
                } else {
                    stats.setFinalizacoesVisitante(stats.getFinalizacoesVisitante() + 1);
                }
                statsJogadorCarreira.setFinalizacoes(statsJogadorCarreira.getFinalizacoes() + 1);
                statsJogadorCampeonato.setFinalizacoes(statsJogadorCampeonato.getFinalizacoes() + 1);
            }

            case GOL, PENALTI_GOL -> {
                if (mandante) {
                    stats.setFinalizacoesMandante(stats.getFinalizacoesMandante() + 1);
                    stats.setFinalizacoesGolMandante(stats.getFinalizacoesGolMandante() + 1);
                } else {
                    stats.setFinalizacoesVisitante(stats.getFinalizacoesVisitante() + 1);
                    stats.setFinalizacoesGolVisitante(stats.getFinalizacoesGolVisitante() + 1);
                }

                statsJogadorCarreira.setFinalizacoes(statsJogadorCarreira.getFinalizacoes() + 1);
                statsJogadorCarreira.setGols(statsJogadorCarreira.getGols() + 1);

                statsJogadorCampeonato.setFinalizacoes(statsJogadorCampeonato.getFinalizacoes() + 1);
                statsJogadorCampeonato.setGols(statsJogadorCampeonato.getGols() + 1);

                // assistência
                if (e.getJogadorSecundario() != null) {

                    EstatisticasJogador assistCarreira = estatisticasJogadorRepository
                            .findByJogadorId(e.getJogadorSecundario().getId())
                            .orElseGet(() -> {
                                EstatisticasJogador s = new EstatisticasJogador();
                                s.setJogador(e.getJogadorSecundario());
                                return estatisticasJogadorRepository.saveAndFlush(s);
                            });

                    EstatisticasJogadorCampeonato assistCampeonato = estatisticasJogadorCampeonatoRepository
                            .findByJogadorIdAndCampeonatoId(
                                    e.getJogadorSecundario().getId(),
                                    partida.getCampeonato().getId()
                            )
                            .orElseGet(() -> {
                                EstatisticasJogadorCampeonato s = new EstatisticasJogadorCampeonato();
                                s.setJogador(e.getJogadorSecundario());
                                s.setCampeonato(partida.getCampeonato());
                                return estatisticasJogadorCampeonatoRepository.saveAndFlush(s);
                            });

                    assistCarreira.setAssistencias(assistCarreira.getAssistencias() + 1);
                    assistCampeonato.setAssistencias(assistCampeonato.getAssistencias() + 1);

                    estatisticasJogadorRepository.save(assistCarreira);
                    estatisticasJogadorCampeonatoRepository.save(assistCampeonato);
                }
            }

            case GOL_CONTRA -> {
                if (mandante) {
                    stats.setFinalizacoesGolVisitante(stats.getFinalizacoesGolVisitante() + 1);
                } else {
                    stats.setFinalizacoesGolMandante(stats.getFinalizacoesGolMandante() + 1);
                }
            }

            case FALTA -> {
                if (mandante) {
                    stats.setFaltasMandante(stats.getFaltasMandante() + 1);
                } else {
                    stats.setFaltasVisitante(stats.getFaltasVisitante() + 1);
                }
                statsJogadorCarreira.setFaltasCometidas(statsJogadorCarreira.getFaltasCometidas() + 1);
                statsJogadorCampeonato.setFaltasCometidas(statsJogadorCampeonato.getFaltasCometidas() + 1);
            }

            case CARTAO_AMARELO -> {
                if (mandante) {
                    stats.setCartoesAmarelosMandante(stats.getCartoesAmarelosMandante() + 1);
                } else {
                    stats.setCartoesAmarelosVisitante(stats.getCartoesAmarelosVisitante() + 1);
                }
                statsJogadorCarreira.setCartoesAmarelos(statsJogadorCarreira.getCartoesAmarelos() + 1);
                statsJogadorCampeonato.setCartoesAmarelos(statsJogadorCampeonato.getCartoesAmarelos() + 1);

                int amarelosParaSuspensao = partida.getCampeonato().getAmarelosParaSuspensao() != null
                        ? partida.getCampeonato().getAmarelosParaSuspensao()
                        : 3;

                if (statsJogadorCampeonato.getCartoesAmarelos() >= amarelosParaSuspensao) {
                    int proximaRodada = partida.getRodada() + 1;

                    boolean jaTemSuspensao = suspensaoRepository
                            .existsByJogadorIdAndCampeonatoIdAndRodadaSuspensao(
                                    e.getJogador().getId(),
                                    partida.getCampeonato().getId(),
                                    proximaRodada
                            );

                    if (!jaTemSuspensao) {
                        Suspensao suspensao = new Suspensao();
                        suspensao.setJogador(e.getJogador());
                        suspensao.setCampeonato(partida.getCampeonato());
                        suspensao.setRodadaSuspensao(proximaRodada);
                        suspensao.setMotivo(MotivoSuspensao.ACUMULO_AMARELOS);
                        suspensaoRepository.save(suspensao);

                        statsJogadorCampeonato.setCartoesAmarelos(0);
                    }
                }
            }
            case CARTAO_VERMELHO -> {
                if (mandante) {
                    stats.setCartoesVermelhosMandante(stats.getCartoesVermelhosMandante() + 1);
                } else {
                    stats.setCartoesVermelhosVisitante(stats.getCartoesVermelhosVisitante() + 1);
                }
                statsJogadorCarreira.setCartoesVermelhos(statsJogadorCarreira.getCartoesVermelhos() + 1);
                statsJogadorCampeonato.setCartoesVermelhos(statsJogadorCampeonato.getCartoesVermelhos() + 1);

                int proximaRodada = partida.getRodada() + 1;

                boolean jaTemSuspensao = suspensaoRepository
                        .existsByJogadorIdAndCampeonatoIdAndRodadaSuspensao(
                                e.getJogador().getId(),
                                partida.getCampeonato().getId(),
                                proximaRodada
                        );

                if (!jaTemSuspensao) {
                    Suspensao suspensao = new Suspensao();
                    suspensao.setJogador(e.getJogador());
                    suspensao.setCampeonato(partida.getCampeonato());
                    suspensao.setRodadaSuspensao(proximaRodada);
                    suspensao.setMotivo(MotivoSuspensao.CARTAO_VERMELHO);
                    suspensaoRepository.save(suspensao);
                }
            }

            case ESCANTEIO -> {
                if (mandante) {
                    stats.setEscanteiosMandante(stats.getEscanteiosMandante() + 1);
                } else {
                    stats.setEscanteiosVisitante(stats.getEscanteiosVisitante() + 1);
                }
            }

            case DEFESA -> {
                if (mandante) {
                    stats.setDefesasMandante(stats.getDefesasMandante() + 1);
                } else {
                    stats.setDefesasVisitante(stats.getDefesasVisitante() + 1);
                }
                statsJogadorCarreira.setDefesas(statsJogadorCarreira.getDefesas() + 1);
                statsJogadorCampeonato.setDefesas(statsJogadorCampeonato.getDefesas() + 1);
            }

            case PENALTI_DEFENDIDO -> {
                if (mandante) {
                    stats.setPenaltisDefendidosMandante(stats.getPenaltisDefendidosMandante() + 1);
                } else {
                    stats.setPenaltisDefendidosVisitante(stats.getPenaltisDefendidosVisitante() + 1);
                }
                statsJogadorCarreira.setPenaltisDefendidos(statsJogadorCarreira.getPenaltisDefendidos() + 1);
                statsJogadorCampeonato.setPenaltisDefendidos(statsJogadorCampeonato.getPenaltisDefendidos() + 1);
            }

            case PENALTI_PERDIDO -> {
                statsJogadorCarreira.setPenaltisPerdidos(statsJogadorCarreira.getPenaltisPerdidos() + 1);
                statsJogadorCampeonato.setPenaltisPerdidos(statsJogadorCampeonato.getPenaltisPerdidos() + 1);
            }

            case IMPEDIMENTO,
                 VAR_GOL_CONFIRMADO, VAR_GOL_ANULADO,
                 INICIO_PRIMEIRO_TEMPO, FIM_PRIMEIRO_TEMPO,
                 INICIO_SEGUNDO_TEMPO, FIM_PARTIDA -> {
            }
            case SUBSTITUICAO -> {

                if (e.getJogador() == null || e.getJogadorSecundario() == null) {
                    break;
                }

                // jogador que sai
                escalacaoPartidaRepository
                        .findByPartidaIdAndJogadorId(
                                partida.getId(),
                                e.getJogador().getId()
                        )
                        .ifPresent(esc -> {
                            esc.setAtivo(false);
                            escalacaoPartidaRepository.save(esc);
                        });

                // jogador que entra
                escalacaoPartidaRepository
                        .findByPartidaIdAndJogadorId(
                                partida.getId(),
                                e.getJogadorSecundario().getId()
                        )
                        .ifPresent(esc -> {
                            esc.setAtivo(true);
                            escalacaoPartidaRepository.save(esc);
                        });
                // stats da partida
                if (mandante) {
                    stats.setSubstituicoesMandante(
                            stats.getSubstituicoesMandante() + 1
                    );
                } else {
                    stats.setSubstituicoesVisitante(
                            stats.getSubstituicoesVisitante() + 1
                    );
                }
            }
        }
            repository.save(stats);

            if (statsJogadorCarreira != null) {
                estatisticasJogadorRepository.save(statsJogadorCarreira);
            }
            if (statsJogadorCampeonato != null) {
                estatisticasJogadorCampeonatoRepository.save(statsJogadorCampeonato);
            }
        }
    }
