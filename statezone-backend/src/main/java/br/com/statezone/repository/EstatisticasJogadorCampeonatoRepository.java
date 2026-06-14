package br.com.statezone.repository;

import br.com.statezone.model.EstatisticasJogadorCampeonato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstatisticasJogadorCampeonatoRepository
        extends JpaRepository<EstatisticasJogadorCampeonato, Long> {

    Optional<EstatisticasJogadorCampeonato> findByJogadorIdAndCampeonatoId(
            Long jogadorId,
            Long campeonatoId
    );

    @Query("""
        SELECT e FROM EstatisticasJogadorCampeonato e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        WHERE e.campeonato.id = :campeonatoId
          AND e.gols > 0
        ORDER BY e.gols DESC
    """)
    List<EstatisticasJogadorCampeonato> findArtilheirosByCampeonatoId(
            @Param("campeonatoId") Long campeonatoId
    );

    @Query("""
        SELECT e FROM EstatisticasJogadorCampeonato e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        WHERE e.campeonato.id = :campeonatoId
          AND e.assistencias > 0
        ORDER BY e.assistencias DESC
    """)
    List<EstatisticasJogadorCampeonato> findAssistentesByCampeonatoId(
            @Param("campeonatoId") Long campeonatoId
    );

    @Query("""
        SELECT e FROM EstatisticasJogadorCampeonato e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        WHERE e.campeonato.id = :campeonatoId
          AND e.cartoesAmarelos > 0
        ORDER BY e.cartoesAmarelos DESC
    """)
    List<EstatisticasJogadorCampeonato> findCartoesAmarelosByCampeonatoId(
            @Param("campeonatoId") Long campeonatoId
    );

    @Query("""
        SELECT e FROM EstatisticasJogadorCampeonato e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        WHERE e.campeonato.id = :campeonatoId
          AND e.cartoesVermelhos > 0
        ORDER BY e.cartoesVermelhos DESC
    """)
    List<EstatisticasJogadorCampeonato> findCartoesVermelhosByCampeonatoId(
            @Param("campeonatoId") Long campeonatoId
    );

    @Query("""
        SELECT e FROM EstatisticasJogadorCampeonato e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        WHERE e.campeonato.id = :campeonatoId
          AND e.partidasJogadas >= :minPartidas
    """)
    List<EstatisticasJogadorCampeonato> findParaDestaques(
            @Param("campeonatoId") Long campeonatoId,
            @Param("minPartidas") int minPartidas
    );

    @Query("""
    SELECT e FROM EstatisticasJogadorCampeonato e
    JOIN FETCH e.jogador j
    JOIN FETCH j.time t
    WHERE e.campeonato.id = :campeonatoId
      AND j.posicao = br.com.statezone.enums.Posicao.GOLEIRO
    ORDER BY e.cleanSheets DESC, e.defesas DESC, e.penaltisDefendidos DESC
    """)
    List<EstatisticasJogadorCampeonato> findRankingGoleirosByCampeonatoId(
            @Param("campeonatoId") Long campeonatoId
    );
}