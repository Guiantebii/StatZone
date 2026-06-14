package br.com.statezone.repository;

import br.com.statezone.model.Suspensao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuspensaoRepository extends JpaRepository<Suspensao, Long> {

    List<Suspensao> findByCampeonatoIdAndRodadaSuspensao(
            Long campeonatoId,
            Integer rodadaSuspensao
    );

    boolean existsByJogadorIdAndCampeonatoIdAndRodadaSuspensao(
            Long jogadorId,
            Long campeonatoId,
            Integer rodadaSuspensao
    );
}