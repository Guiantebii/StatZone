package br.com.statezone.repository;

import br.com.statezone.model.Campeonato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampeonatoRepository extends JpaRepository<Campeonato,Long> {
    Optional<Campeonato> findByApiFootballId(Long apiFootballId);
}
