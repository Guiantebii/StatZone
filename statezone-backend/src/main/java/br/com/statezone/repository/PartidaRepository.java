package br.com.statezone.repository;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.EventoPartida;
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

    @Query("SELECT p FROM Partida p " +
            "JOIN FETCH p.timeMandante " +
            "JOIN FETCH p.timeVisitante " +
            "WHERE p.campeonato.id = :campeonatoId " +
            "AND p.status IN :statuses")
    List<Partida> findByCampeonatoIdAndStatusInWithTimes(
            @Param("campeonatoId") Long campeonatoId,
            @Param("statuses") List<StatusPartida> statuses
    );

    @Query("""
    SELECT MIN(p.rodada) FROM Partida p
    WHERE p.campeonato.id = :campeonatoId
      AND p.status IN (
          br.com.statezone.enums.StatusPartida.AGENDADA,
          br.com.statezone.enums.StatusPartida.AO_VIVO
      )
    """)
    Integer findProximaRodada(@Param("campeonatoId") Long campeonatoId);

    @Query("""
    SELECT MAX(p.rodada) FROM Partida p
    WHERE p.campeonato.id = :campeonatoId
    """)
    Integer findMaxRodada(@Param("campeonatoId") Long campeonatoId);

    boolean existsByGrupoId(Long grupoId);
}
