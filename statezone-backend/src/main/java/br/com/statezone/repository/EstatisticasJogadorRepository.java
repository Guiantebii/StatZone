package br.com.statezone.repository;

import br.com.statezone.model.EstatisticasJogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstatisticasJogadorRepository extends JpaRepository<EstatisticasJogador,Long> {
    Optional<EstatisticasJogador> findByJogadorId(Long JogadorId);

    @Query("""
        SELECT e FROM EstatisticasJogador e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        JOIN EventoPartida ep ON ep.jogador.id = j.id
        JOIN ep.partida p
        WHERE p.campeonato.id = :campeonatoId
        AND ep.tipoEvento IN ('GOL', 'PENALTI_GOL')
        GROUP BY e.id, j.id, t.id
        ORDER BY e.gols DESC
        """)
    List<EstatisticasJogador> findArtilhariasByCampeonatoId(@Param("campeonatoId") Long campeonatoId);
}
