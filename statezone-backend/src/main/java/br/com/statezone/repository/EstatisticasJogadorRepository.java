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
        SELECT DISTINCT e FROM EstatisticasJogador e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        JOIN EventoPartida ep ON ep.jogador.id = j.id
        JOIN ep.partida p
        WHERE p.campeonato.id = :campeonatoId
          AND e.gols > 0
        ORDER BY e.gols DESC
        """)
    List<EstatisticasJogador> findArtilheirosByCampeonatoId(@Param("campeonatoId") Long campeonatoId);

    @Query("""
        SELECT DISTINCT e FROM EstatisticasJogador e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        JOIN EventoPartida ep ON ep.jogador.id = j.id
        JOIN ep.partida p
        WHERE p.campeonato.id = :campeonatoId
          AND e.assistencias > 0
        ORDER BY e.assistencias DESC
        """)
    List<EstatisticasJogador> findAssistentesByCampeonatoId(@Param("campeonatoId") Long campeonatoId);

    @Query("""
        SELECT DISTINCT e FROM EstatisticasJogador e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        JOIN EventoPartida ep ON ep.jogador.id = j.id
        JOIN ep.partida p
        WHERE p.campeonato.id = :campeonatoId
          AND e.cartoesAmarelos > 0
        ORDER BY e.cartoesAmarelos DESC
        """)
    List<EstatisticasJogador> findCartoesAmarelosByCampeonatoId(@Param("campeonatoId") Long campeonatoId);

    @Query("""
        SELECT DISTINCT e FROM EstatisticasJogador e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        JOIN EventoPartida ep ON ep.jogador.id = j.id
        JOIN ep.partida p
        WHERE p.campeonato.id = :campeonatoId
          AND e.cartoesVermelhos > 0
        ORDER BY e.cartoesVermelhos DESC
        """)
    List<EstatisticasJogador> findCartoesVermelhosByCampeonatoId(@Param("campeonatoId") Long campeonatoId);
}
