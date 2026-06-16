package br.com.statezone.repository;

import br.com.statezone.enums.FaseEnum;
import br.com.statezone.model.FaseEliminatoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaseEliminatoriaRepository extends JpaRepository<FaseEliminatoria, Long> {

    List<FaseEliminatoria> findByCampeonatoId(Long campeonatoId);

    Optional<FaseEliminatoria> findByCampeonatoIdAndFase(Long campeonatoId, FaseEnum fase);

    boolean existsByCampeonatoIdAndFase(Long campeonatoId, FaseEnum fase);
}