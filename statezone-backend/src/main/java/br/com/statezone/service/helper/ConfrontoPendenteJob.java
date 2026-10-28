package br.com.statezone.service.helper;

import br.com.statezone.model.ProcessamentoConfrontoPendente;
import br.com.statezone.repository.ProcessamentoConfrontoPendenteRepository;
import br.com.statezone.service.PartidaEncerradaProcessor;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final PartidaEncerradaProcessor processor;
    private final MeterRegistry meterRegistry;

    private static final int MAX_TENTATIVAS = 3;

    @Scheduled(fixedDelay = 60000)
    public void processarPendentes() {
        List<ProcessamentoConfrontoPendente> pendentes =
                pendenteRepository.findByResolvidoFalseAndTentativasLessThan(MAX_TENTATIVAS);

        for (ProcessamentoConfrontoPendente pendente : pendentes) {
            try {
                processor.processar(pendente.getPartidaId());
            } catch (Exception e) {
                registrarFalha(pendente.getId(), e);
            }
        }
    }

    private void registrarFalha(Long pendenteId, Exception erro) {
        pendenteRepository.findById(pendenteId).ifPresent(pendente -> {
            pendente.setTentativas(pendente.getTentativas() + 1);
            String mensagem = erro.getMessage() == null ? erro.getClass().getSimpleName() : erro.getMessage();
            pendente.setUltimoErro(mensagem);
            pendenteRepository.save(pendente);
            meterRegistry.counter("statzone.partidas.processamento.falhas").increment();
            if (pendente.getTentativas() >= MAX_TENTATIVAS) {
                log.error("Falha definitiva ao processar partida {}", pendente.getPartidaId(), erro);
            }
        });
    }
}
