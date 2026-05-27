package br.com.statezone.repository;

import br.com.statezone.model.EventoPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoPartidaRepository extends JpaRepository<EventoPartida,Long> {
    List<EventoPartida> findByPartidaIdOrderByMinutoAscMinutoExtraAsc(Long partidaId);
    List<EventoPartida> findByPartida_Id(Long partidaId);
}
