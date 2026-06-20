package br.com.statezone.repository;

import br.com.statezone.model.ProcessamentoConfrontoPendente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProcessamentoConfrontoPendenteRepository
        extends JpaRepository<ProcessamentoConfrontoPendente, Long> {

    List<ProcessamentoConfrontoPendente>
    findByResolvidoFalseAndTentativasLessThan(int maxTentativas);

    Optional<ProcessamentoConfrontoPendente> findByPartidaId(Long partidaId);
}