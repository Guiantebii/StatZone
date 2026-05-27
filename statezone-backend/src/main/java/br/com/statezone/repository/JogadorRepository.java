package br.com.statezone.repository;

import br.com.statezone.model.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador,Long> {
    List<Jogador> findByTimeId(Long timeId);
}
