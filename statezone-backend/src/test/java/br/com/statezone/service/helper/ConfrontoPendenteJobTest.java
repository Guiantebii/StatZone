package br.com.statezone.service.helper;

import br.com.statezone.model.ProcessamentoConfrontoPendente;
import br.com.statezone.repository.ProcessamentoConfrontoPendenteRepository;
import br.com.statezone.service.PartidaEncerradaProcessor;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfrontoPendenteJobTest {

    @Mock
    private ProcessamentoConfrontoPendenteRepository pendenteRepository;

    @Mock
    private PartidaEncerradaProcessor processor;

    @Test
    void falhaNoProcessamento_incrementaTentativa() {
        ProcessamentoConfrontoPendente pendente = ProcessamentoConfrontoPendente.builder()
                .id(4L)
                .partidaId(8L)
                .tentativas(0)
                .resolvido(false)
                .build();
        when(pendenteRepository.findByResolvidoFalseAndTentativasLessThan(3)).thenReturn(List.of(pendente));
        when(pendenteRepository.findById(4L)).thenReturn(Optional.of(pendente));
        doThrow(new IllegalStateException("falha")).when(processor).processar(8L);

        new ConfrontoPendenteJob(pendenteRepository, processor, new SimpleMeterRegistry()).processarPendentes();

        assertThat(pendente.getTentativas()).isEqualTo(1);
        assertThat(pendente.getUltimoErro()).isEqualTo("falha");
        verify(pendenteRepository).save(pendente);
    }
}
