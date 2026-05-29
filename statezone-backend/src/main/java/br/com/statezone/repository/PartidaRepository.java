package br.com.statezone.repository;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida,Long> {
    List<Partida> findByCampeonatoIdAndStatus(
            Long campeonatoId,
            StatusPartida status
    );
    boolean existsByCampeonatoId(Long campeonatoId);

    @Query("SELECT p FROM Partida p " +
            "JOIN FETCH p.timeMandante " +
            "JOIN FETCH p.timeVisitante " +
            "WHERE p.campeonato.id = :campeonatoId AND p.status = :status")
    List<Partida> findByCampeonatoIdAndStatusWithTimes(
            @Param("campeonatoId") Long campeonatoId,
            @Param("status") StatusPartida status
    );
    List<Partida> findByCampeonatoId(Long campeonatoId);

    long countByCampeonatoIdAndStatus(Long campeonatoId, StatusPartida status);
}
