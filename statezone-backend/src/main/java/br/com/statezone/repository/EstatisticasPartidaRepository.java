package br.com.statezone.repository;

import br.com.statezone.model.EstatisticasPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstatisticasPartidaRepository extends JpaRepository<EstatisticasPartida,Long> {
    Optional<EstatisticasPartida> findByPartidaId(Long partidaId);
}
