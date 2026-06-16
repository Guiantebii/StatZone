package br.com.statezone.service;

import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.model.*;
import br.com.statezone.repository.ConfrontoEliminatorioRepository;
import br.com.statezone.repository.FaseEliminatoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BracketServiceEngine {

    private final ConfrontoEliminatorioRepository confrontoRepository;
    private final FaseEliminatoriaRepository faseRepository;


    public void gerarFase(FaseEliminatoria fase, List<Time> times) {

        FaseEnum faseEnum = fase.getFase();

        int totalConfrontos = calcularConfrontos(faseEnum);
        int chaveAtual = 1;

        List<ConfrontoEliminatorio> confrontos = new ArrayList<>();

        for (int i = 0; i < totalConfrontos; i++) {

            ConfrontoEliminatorio confronto = new ConfrontoEliminatorio();

            confronto.setFaseEliminatoria(fase);
            confronto.setStatusConfronto(StatusConfronto.PENDENTE);

            confronto.setOrdem(i + 1);

            if (i < totalConfrontos / 2) {
                confronto.setChave(1);
            } else {
                confronto.setChave(2);
            }


            if (times != null && times.size() >= totalConfrontos * 2) {
                confronto.setTimeA(times.get(i * 2));
                confronto.setTimeB(times.get(i * 2 + 1));
            }

            confrontos.add(confronto);
        }

        confrontoRepository.saveAll(confrontos);
    }

    private int calcularConfrontos(FaseEnum fase) {

        return switch (fase) {
            case OITAVAS -> 8;
            case QUARTAS -> 4;
            case SEMIFINAL -> 2;
            case FINAL -> 1;
            case TERCEIRO_LUGAR -> 1;
        };
    }
}