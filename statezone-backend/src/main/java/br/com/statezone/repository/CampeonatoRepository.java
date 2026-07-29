package br.com.statezone.repository;

import br.com.statezone.enums.StatusCampeonato;
import br.com.statezone.model.Campeonato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampeonatoRepository extends JpaRepository<Campeonato,Long> {
    Optional<Campeonato> findByApiFootballId(Long apiFootballId);

    @Query("SELECT c FROM Campeonato c LEFT JOIN FETCH c.times")
    List<Campeonato> findAllWithTimes();

    @Query("SELECT c FROM Campeonato c LEFT JOIN FETCH c.times WHERE c.id = :id")
    Optional<Campeonato> findByIdWithTimes(@Param("id") Long id);

    List<Campeonato> findByStatus(StatusCampeonato status);
}
