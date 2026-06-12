package br.com.statezone.repository;

import br.com.statezone.model.EscalacaoPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EscalacaoPartidaRepository extends JpaRepository<EscalacaoPartida, Long> {
    List<EscalacaoPartida> findByPartidaId(Long partidaId);

    Optional<EscalacaoPartida> findByPartidaIdAndJogadorId(Long partidaId, Long jogadorId);

    @Query("""
        SELECT e FROM EscalacaoPartida e
        JOIN FETCH e.jogador j
        JOIN FETCH j.time t
        WHERE e.partida.id = :partidaId
        ORDER BY e.funcao ASC, e.numeroCamisa ASC
    """)
    List<EscalacaoPartida> findByPartidaIdWithJogador(@Param("partidaId") Long partidaId);

    boolean existsByPartidaIdAndJogadorId(Long partidaId, Long jogadorId);
}
