package br.com.statezone.service.helper;

import br.com.statezone.listeners.ConfrontoEventListener;
import br.com.statezone.model.Partida;
import br.com.statezone.model.ProcessamentoConfrontoPendente;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.ProcessamentoConfrontoPendenteRepository;
import org.springframework.transaction.annotation.Transactional;
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
                Partida partida = partidaRepository.findById(p.getPartidaId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Partida não encontrada com ID: " + p.getPartidaId()));

                confrontoEventListener.processarConfronto(partida);

            } catch (IllegalArgumentException e) {
                log.warn("Partida não encontrada no processamento de confronto: {}", e.getMessage());
                p.setResolvido(true);
                pendenteRepository.save(p);
            } catch (Exception e) {
                p.setTentativas(p.getTentativas() + 1);
                p.setUltimoErro("Erro ao processar confronto pendente: " + e.getMessage());
if (p.getTentativas() >= MAX_TENTATIVAS) {
                    log.error("Falha definitiva ao processar confronto da partida {}", p.getPartidaId(), e);
                }
                pendenteRepository.save(p);
            }
        }
    }
}