package br.com.statezone.service.ranking;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.service.helper.ClassificacaoStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RankingEngine {

    private final PartidaRepository partidaRepository;

    private static final List<StatusPartida> STATUSES_QUE_CONTAM =
            List.of(
                    StatusPartida.ENCERRADA,
                    StatusPartida.WO_MANDANTE,
                    StatusPartida.WO_VISITANTE
            );

    public List<ClassificacaoStats> gerar(Long campeonatoId) {
        return gerarComFiltro(campeonatoId, null);
    }

    public List<ClassificacaoStats> gerarPorTurno(Long campeonatoId, Integer turno) {
        return gerarComFiltro(campeonatoId, turno);
    }

    public List<ClassificacaoStats> gerarPorGrupo(Long grupoId) {
        List<Partida> partidas =
                partidaRepository.findByGrupoIdAndStatusIn(grupoId, STATUSES_QUE_CONTAM);
        return calcularClassificacao(partidas);
    }

    private List<ClassificacaoStats> gerarComFiltro(Long campeonatoId, Integer turno) {

        List<Partida> partidas =
                partidaRepository.findByCampeonatoIdAndStatusInWithTimes(
                        campeonatoId,
                        STATUSES_QUE_CONTAM
                );

        if (turno != null) {
            Integer maxRodada = partidaRepository.findMaxRodada(campeonatoId);

            if (maxRodada != null) {
                int rodadasPorTurno = maxRodada / 2;

                partidas = partidas.stream()
                        .filter(p -> {
                            if (turno == 1) return p.getRodada() <= rodadasPorTurno;
                            if (turno == 2) return p.getRodada() > rodadasPorTurno;
                            return true;
                        })
                        .toList();
            }
        }

        return calcularClassificacao(partidas);
    }

    private List<ClassificacaoStats> calcularClassificacao(List<Partida> partidas) {

        Map<Long, ClassificacaoStats> tabela = new HashMap<>();

        for (Partida partida : partidas) {

            Time mandante = partida.getTimeMandante();
            Time visitante = partida.getTimeVisitante();

            ClassificacaoStats statsMandante =
                    tabela.computeIfAbsent(
                            mandante.getId(),
                            id -> new ClassificacaoStats(mandante)
                    );

            ClassificacaoStats statsVisitante =
                    tabela.computeIfAbsent(
                            visitante.getId(),
                            id -> new ClassificacaoStats(visitante)
                    );

            int golsMandante = partida.getGolsMandante();
            int golsVisitante = partida.getGolsVisitante();

            // jogos
            statsMandante.setJogos(statsMandante.getJogos() + 1);
            statsVisitante.setJogos(statsVisitante.getJogos() + 1);

            // gols
            statsMandante.setGolsFeitos(statsMandante.getGolsFeitos() + golsMandante);
            statsMandante.setGolsSofridos(statsMandante.getGolsSofridos() + golsVisitante);

            statsVisitante.setGolsFeitos(statsVisitante.getGolsFeitos() + golsVisitante);
            statsVisitante.setGolsSofridos(statsVisitante.getGolsSofridos() + golsMandante);

            statsMandante.setSaldoGols(
                    statsMandante.getGolsFeitos() - statsMandante.getGolsSofridos()
            );

            statsVisitante.setSaldoGols(
                    statsVisitante.getGolsFeitos() - statsVisitante.getGolsSofridos()
            );

            // resultado
            if (golsMandante > golsVisitante) {

                statsMandante.setVitorias(statsMandante.getVitorias() + 1);
                statsMandante.setPontos(statsMandante.getPontos() + 3);
                statsVisitante.setDerrotas(statsVisitante.getDerrotas() + 1);

            } else if (golsVisitante > golsMandante) {

                statsVisitante.setVitorias(statsVisitante.getVitorias() + 1);
                statsVisitante.setPontos(statsVisitante.getPontos() + 3);
                statsMandante.setDerrotas(statsMandante.getDerrotas() + 1);

            } else {

                statsMandante.setEmpates(statsMandante.getEmpates() + 1);
                statsVisitante.setEmpates(statsVisitante.getEmpates() + 1);
                statsMandante.setPontos(statsMandante.getPontos() + 1);
                statsVisitante.setPontos(statsVisitante.getPontos() + 1);
            }
        }

        // ordenação
        List<ClassificacaoStats> ranking =
                tabela.values()
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(ClassificacaoStats::getPontos, Comparator.reverseOrder())
                                        .thenComparing(ClassificacaoStats::getSaldoGols, Comparator.reverseOrder())
                                        .thenComparing(ClassificacaoStats::getGolsFeitos, Comparator.reverseOrder())
                        )
                        .collect(Collectors.toList());

        for (int i = 0; i < ranking.size(); i++) {
            ClassificacaoStats stats = ranking.get(i);
            stats.setPosicao(i + 1);

            double aproveitamento = stats.getJogos() > 0
                    ? Math.round((stats.getPontos() / (double)(stats.getJogos() * 3)) * 1000.0) / 10.0
                    : 0.0;
            stats.setAproveitamento(aproveitamento);
        }
        return ranking;
    }
}