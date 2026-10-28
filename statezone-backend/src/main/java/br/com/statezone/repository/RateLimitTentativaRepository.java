package br.com.statezone.repository;

import br.com.statezone.model.RateLimitTentativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RateLimitTentativaRepository extends JpaRepository<RateLimitTentativa, Long> {

    long countByChaveAndInstanteAfter(String chave, Instant instante);

    void deleteByInstanteBefore(Instant instante);
}
