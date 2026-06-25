package br.com.statezone.repository;

import br.com.statezone.model.Time;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeRepository extends JpaRepository<Time, Long> {
    Optional<Time> findByApiFootballId(Long apiFootballId);

    List<Time> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT COUNT(c) FROM Campeonato c JOIN c.times t WHERE t.id = :timeId")
    int countCampeonatosByTimeId(@Param("timeId") Long timeId);
}
