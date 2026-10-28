package br.com.statezone.security;

import br.com.statezone.model.RateLimitTentativa;
import br.com.statezone.repository.RateLimitTentativaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rate-limiting.store", havingValue = "jdbc", matchIfMissing = true)
public class JdbcRateLimitStore implements RateLimitStore {

    private static final int MAX_POR_MINUTO = 5;
    private static final int MAX_EM_BLOQUEIO = 15;

    private final RateLimitTentativaRepository repository;

    @Override
    @Transactional
    public boolean permitir(String chave) {
        Instant agora = Instant.now();
        long recentes = repository.countByChaveAndInstanteAfter(chave, agora.minus(15, ChronoUnit.MINUTES));
        if (recentes >= MAX_EM_BLOQUEIO) {
            return false;
        }
        long noMinuto = repository.countByChaveAndInstanteAfter(chave, agora.minus(60, ChronoUnit.SECONDS));
        if (noMinuto >= MAX_POR_MINUTO) {
            return false;
        }
        RateLimitTentativa tentativa = new RateLimitTentativa();
        tentativa.setChave(chave);
        tentativa.setInstante(agora);
        repository.save(tentativa);
        return true;
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void limpar() {
        repository.deleteByInstanteBefore(Instant.now().minus(15, ChronoUnit.MINUTES));
    }
}
