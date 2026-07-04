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
import java.util.Optional;

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
      AND p.status = 'ENCERRADA'
    """)
    Integer findMaxRodada(@Param("campeonatoId") Long campeonatoId);

    boolean existsByGrupoId(Long grupoId);

    Optional<Partida> findByApiFootballId(Long apiFootballId);

    @Query("""
    SELECT p FROM Partida p
    WHERE p.campeonato.id = :campeonatoId
      AND p.status = 'AGENDADA'
      AND (p.timeMandante.id = :timeId OR p.timeVisitante.id = :timeId)
    ORDER BY p.rodada ASC NULLS LAST, p.id ASC
""")
    List<Partida> findProximasPartidasDoTime(Long campeonatoId, Long timeId, Pageable pageable);

    List<Partida> findByGrupoIdAndStatusIn(Long grupoId, List<StatusPartida> status);

    @Query("""
    SELECT p FROM Partida p
    JOIN FETCH p.timeMandante
    JOIN FETCH p.timeVisitante
    WHERE (p.timeMandante.id = :timeId OR p.timeVisitante.id = :timeId)
    AND p.status IN ('AGENDADA', 'AO_VIVO', 'INTERVALO', 'PENALTIS')
    ORDER BY p.dataPartida ASC
    """)
    List<Partida> findProximasPartidas(
            @Param("timeId") Long timeId,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM Partida p
    JOIN FETCH p.timeMandante
    JOIN FETCH p.timeVisitante
    WHERE (p.timeMandante.id = :timeId OR p.timeVisitante.id = :timeId)
    AND p.status = br.com.statezone.enums.StatusPartida.ENCERRADA
    ORDER BY p.dataPartida DESC
    """)
    List<Partida> findUltimasPartidasComTimes(
            @Param("timeId") Long timeId,
            Pageable pageable
    );
}
