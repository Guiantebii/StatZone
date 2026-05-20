package br.com.statezone.repository;

import br.com.statezone.model.EstatisticasPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstatisticasPartidaRepository extends JpaRepository<EstatisticasPartida,Long> {
}
