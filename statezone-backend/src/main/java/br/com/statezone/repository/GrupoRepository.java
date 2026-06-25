package br.com.statezone.repository;

import br.com.statezone.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    List<Grupo> findByCampeonatoId(Long campeonatoId);

    Optional<Grupo> findByCampeonatoIdAndNome(Long campeonatoId, String nome);

    boolean existsByCampeonatoIdAndNome(Long campeonatoId, String nome);

    @Query("""
        SELECT g FROM Grupo g
        LEFT JOIN FETCH g.times t
        WHERE g.campeonato.id = :campeonatoId
    """)
    List<Grupo> findByCampeonatoIdWithTimes(@Param("campeonatoId") Long campeonatoId);

    @Query("""
    SELECT COUNT(g) > 0
    FROM Grupo g
    JOIN g.times t
    WHERE g.campeonato.id = :campeonatoId
    AND t.id = :timeId
""")
    boolean existsTimeEmAlgumGrupoDoCampeonato(
            @Param("campeonatoId") Long campeonatoId,
            @Param("timeId") Long timeId
    );
    int countByCampeonatoId(Long campeonatoId);
}