package br.com.statezone.service;

import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.FaseEliminatoria;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class BracketEngine {

    public List<ConfrontoEliminatorio> gerarFaseInicial(List<Time> times, FaseEliminatoria fase) {
        validarListaDeTimes(times);

        List<Time> timesOrdenados = new ArrayList<>(times);

        timesOrdenados.sort(Comparator.comparing(Time::getSeed, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(Time::getId));

        List<ConfrontoEliminatorio> confrontos = new ArrayList<>();
        int totalTimes = timesOrdenados.size();
        int totalConfrontos = totalTimes / 2;

        for (int i = 0; i < totalConfrontos; i++) {
            Time timeA = timesOrdenados.get(i);
            Time timeB = timesOrdenados.get(totalTimes - 1 - i);

            ConfrontoEliminatorio confronto = criarConfronto(fase, timeA, timeB, i, fase.getFase().ordinal() + 1);
            confronto.setSeed(i + 1);

            confrontos.add(confronto);
        }

        return confrontos;
    }

    public List<ConfrontoEliminatorio> gerarProximaFase(List<Time> classificados, FaseEliminatoria fase, int round) {
        validarListaDeTimes(classificados);

        List<ConfrontoEliminatorio> novosConfrontos = new ArrayList<>();
        int totalConfrontos = classificados.size() / 2;

        for (int i = 0; i < totalConfrontos; i++) {
            Time timeA = classificados.get(i);
            Time timeB = classificados.get(classificados.size() - 1 - i);

            ConfrontoEliminatorio confronto = criarConfronto(fase, timeA, timeB, i, round);
            novosConfrontos.add(confronto);
        }

        return novosConfrontos;
    }

    public Time resolverVencedor(ConfrontoEliminatorio confronto) {
        Partida ida = confronto.getPartidaIda();
        Partida volta = confronto.getPartidaVolta();

        validarPartidaEncerrada(ida, "Partida de ida");

        int golsA = ida.getGolsMandante() != null ? ida.getGolsMandante() : 0;
        int golsB = ida.getGolsVisitante() != null ? ida.getGolsVisitante() : 0;

        if (volta != null) {
            validarPartidaEncerrada(volta, "Partida de volta");
            golsA += volta.getGolsVisitante();
            golsB += volta.getGolsMandante();
        }

        if (golsA > golsB) return confronto.getTimeA();
        if (golsB > golsA) return confronto.getTimeB();

        return resolverPenaltis(confronto);
    }

    public Optional<ConfrontoEliminatorio> propagarVencedor(ConfrontoEliminatorio atual, Time vencedor) {
        ConfrontoEliminatorio proximoConfronto = atual.getProximoConfronto();

        if (proximoConfronto == null) return Optional.empty();

        if (atual.getSlotProximo() == 0) {
            proximoConfronto.setTimeA(vencedor);
        } else {
            proximoConfronto.setTimeB(vencedor);
        }

        if (proximoConfronto.getTimeA() != null && proximoConfronto.getTimeB() != null) {
            proximoConfronto.setStatusConfronto(StatusConfronto.EM_ANDAMENTO);
        }

        return Optional.of(proximoConfronto);
    }

    private ConfrontoEliminatorio criarConfronto(FaseEliminatoria fase, Time timeA, Time timeB, int bracketIndex, int roundIndex) {
        ConfrontoEliminatorio c = new ConfrontoEliminatorio();
        c.setFaseEliminatoria(fase);
        c.setTimeA(timeA);
        c.setTimeB(timeB);
        c.setBracketIndex(bracketIndex);
        c.setRoundIndex(roundIndex);
        c.setStatusConfronto(StatusConfronto.PENDENTE);
        c.setJogoUnico(fase.getJogoUnico() != null ? fase.getJogoUnico() : true);
        return c;
    }

    private Time resolverPenaltis(ConfrontoEliminatorio confronto) {
        int penaltisA = confronto.getGolsPenaltisA() != null ? confronto.getGolsPenaltisA() : 0;
        int penaltisB = confronto.getGolsPenaltisB() != null ? confronto.getGolsPenaltisB() : 0;

        if (penaltisA > penaltisB) return confronto.getTimeA();
        if (penaltisB > penaltisA) return confronto.getTimeB();

        String idStr = confronto.getId() != null ? confronto.getId().toString() : "N/A";
        throw new BusinessException(
                String.format("Impossível resolver vencedor. Confronto %s empatado inclusive nos pênaltis.", idStr)
        );
    }

    private void validarListaDeTimes(List<Time> times) {
        if (times == null || times.isEmpty()) {
            throw new BusinessException("A lista de times não pode estar vazia.");
        }
        if (times.size() % 2 != 0) {
            throw new BusinessException(
                    String.format("Número inválido de times para chaveamento: %d. É necessário um número par.", times.size())
            );
        }
    }

    private void validarPartidaEncerrada(Partida partida, String nomePartida) {
        if (partida == null) {
            throw new BusinessException(nomePartida + " não encontrada ou não iniciada.");
        }
        if (partida.getStatus() != StatusPartida.ENCERRADA) {
            throw new BusinessException(nomePartida + " ainda não foi finalizada. Conclua a partida antes de encerrar o confronto.");
        }
    }
}