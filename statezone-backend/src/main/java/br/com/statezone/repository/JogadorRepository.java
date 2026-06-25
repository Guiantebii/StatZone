package br.com.statezone.repository;

import br.com.statezone.enums.Posicao;
import br.com.statezone.model.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador,Long> {
    List<Jogador> findByNomeContainingIgnoreCase(String nome);

    List<Jogador> findByTimeId(Long timeId);
    List<Jogador> findByTimeIdIn(List<Long> timeIds);
    List<Jogador> findByTimeIdAndPosicao(Long timeId, Posicao posicao);
    Optional<Jogador> findByApiFootballId(Long apiFootballId);
    long countByTimeId(Long timeId);
}
