package br.com.statezone.service.helper;

import br.com.statezone.listeners.ConfrontoEventListener;
import br.com.statezone.model.Partida;
import br.com.statezone.model.ProcessamentoConfrontoPendente;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.ProcessamentoConfrontoPendenteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfrontoPendenteJob {

    private final ProcessamentoConfrontoPendenteRepository pendenteRepository;
    private final PartidaRepository partidaRepository;
    private final ConfrontoEventListener confrontoEventListener;

    private static final int MAX_TENTATIVAS = 3;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processarPendentes() {
        List<ProcessamentoConfrontoPendente> pendentes =
                pendenteRepository.findByResolvidoFalseAndTentativasLessThan(MAX_TENTATIVAS);

        for (ProcessamentoConfrontoPendente p : pendentes) {
            try {
                Partida partida = partidaRepository.findById(p.getPartidaId()).orElse(null);
                if (partida == null) {
                    p.setResolvido(true);
                    pendenteRepository.save(p);
                    continue;
                }
                confrontoEventListener.processarConfronto(partida);

            } catch (Exception e) {
                p.setTentativas(p.getTentativas() + 1);
                p.setUltimoErro(e.getMessage());
                if (p.getTentativas() >= MAX_TENTATIVAS) {

                    log.error("Falha definitiva ao processar confronto da partida {}", p.getPartidaId(), e);
                }
                pendenteRepository.save(p);
            }
        }
    }
}