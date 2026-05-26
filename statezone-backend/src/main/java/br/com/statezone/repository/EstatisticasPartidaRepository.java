package br.com.statezone.repository;

import br.com.statezone.model.EstatisticasPartida;
import br.com.statezone.model.EventoPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstatisticasPartidaRepository extends JpaRepository<EstatisticasPartida,Long> {

}
