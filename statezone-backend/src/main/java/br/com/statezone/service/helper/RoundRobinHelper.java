package br.com.statezone.service.helper;

import br.com.statezone.model.Time;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Component
public class RoundRobinHelper {

    public void gerarTurno(List<Time> times, int rodadaOffset, BiConsumer<Time, Time> consumer) {
        List<Time> temp = new ArrayList<>(times);
        boolean impar = temp.size() % 2 != 0;
        if (impar) temp.add(null);

        int n = temp.size();
        int totalRodadas = n - 1;
        int jogosPorRodada = n / 2;

        for (int rodada = 1; rodada <= totalRodadas; rodada++) {
            for (int j = 0; j < jogosPorRodada; j++) {
                Time mandante = temp.get(j);
                Time visitante = temp.get(n - 1 - j);
                if (mandante != null && visitante != null) {
                    consumer.accept(mandante, visitante);
                }
            }

            List<Time> rotated = new ArrayList<>(temp);
            Time ultimo = rotated.remove(n - 1);
            rotated.add(1, ultimo);
            temp = rotated;
        }
    }
}
