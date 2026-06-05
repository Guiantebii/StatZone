package br.com.statezone.repository;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Partida;
import org.springframework.data.domain.Pageable;
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

    @Query("""
    SELECT p FROM Partida p
    WHERE (p.timeMandante.id = :timeId OR p.timeVisitante.id = :timeId)
    AND p.status = br.com.statezone.enums.StatusPartida.ENCERRADA
    ORDER BY p.dataPartida DESC
    """)
    List<Partida> findUltimasPartidas(
            @Param("timeId") Long timeId,
            Pageable pageable
    );
}
