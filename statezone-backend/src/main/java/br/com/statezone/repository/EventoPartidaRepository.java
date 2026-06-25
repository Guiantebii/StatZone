package br.com.statezone.repository;

import br.com.statezone.model.EventoPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoPartidaRepository extends JpaRepository<EventoPartida,Long> {
    List<EventoPartida>
    findByPartidaIdOrderByMinutoAscMinutoExtraAscCriadoEmAsc(Long partidaId);

    List<EventoPartida>
    findByPartida_Id(Long partidaId);

        @Query("""
    SELECT COUNT(e)
    FROM EventoPartida e
    WHERE e.partida.id = :partidaId
    AND e.tipoEvento = 'GOL'
    AND e.time.id = :timeId
    """)
    int countGolsByPartidaAndTime(Long partidaId, Long timeId);


    long countByJogadorId(Long jogadorId);
    long countByJogadorSecundarioId(Long jogadorId);

}
