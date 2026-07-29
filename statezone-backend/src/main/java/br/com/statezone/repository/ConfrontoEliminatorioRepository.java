package br.com.statezone.repository;

import br.com.statezone.model.ConfrontoEliminatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfrontoEliminatorioRepository extends JpaRepository<ConfrontoEliminatorio, Long> {

    List<ConfrontoEliminatorio> findByFaseEliminatoriaIdOrderByBracketIndexAsc(Long faseEliminatoriaId);

    @Query("""
        SELECT c FROM ConfrontoEliminatorio c
        JOIN FETCH c.timeA
        JOIN FETCH c.timeB
        LEFT JOIN FETCH c.timeClassificado
        LEFT JOIN FETCH c.partidaIda
        LEFT JOIN FETCH c.partidaVolta
        WHERE c.faseEliminatoria.id = :faseId
        ORDER BY c.bracketIndex ASC
    """)
    List<ConfrontoEliminatorio> findByFaseIdWithDetails(
            @Param("faseId") Long faseId
    );
    @Query("""
        SELECT c FROM ConfrontoEliminatorio c
        LEFT JOIN FETCH c.partidaIda
        LEFT JOIN FETCH c.partidaVolta
        LEFT JOIN FETCH c.timeA
        LEFT JOIN FETCH c.timeB
        WHERE c.partidaIda.id = :partidaId OR c.partidaVolta.id = :partidaId
        ORDER BY c.bracketIndex ASC
    """)
    Optional<ConfrontoEliminatorio> findConfrontoByPartidaId(@Param("partidaId") Long partidaId);

    @Query("""
        SELECT c FROM ConfrontoEliminatorio c
        JOIN FETCH c.timeA
        JOIN FETCH c.timeB
        LEFT JOIN FETCH c.timeClassificado
        WHERE c.faseEliminatoria.campeonato.id = :campeonatoId
        ORDER BY c.bracketIndex ASC
    """)
    List<ConfrontoEliminatorio> findByCampeonatoId(
            @Param("campeonatoId") Long campeonatoId
    );

    @Query("""
    SELECT COUNT(c) > 0
    FROM ConfrontoEliminatorio c
    WHERE c.faseEliminatoria.id = :faseId
    AND (
        (c.timeA.id = :timeAId AND c.timeB.id = :timeBId)
        OR
        (c.timeA.id = :timeBId AND c.timeB.id = :timeAId)
    )
""")
    boolean existsConfrontoNaFase(
            @Param("faseId") Long faseId,
            @Param("timeAId") Long timeAId,
            @Param("timeBId") Long timeBId
    );

    @Query("""
    SELECT COUNT(c) > 0
    FROM ConfrontoEliminatorio c
    WHERE c.faseEliminatoria.id = :faseId
    AND (
        c.timeA.id = :timeId
        OR c.timeB.id = :timeId
    )
    """)
    boolean existsTimeNaFase(
            @Param("faseId") Long faseId,
            @Param("timeId") Long timeId
    );
}
