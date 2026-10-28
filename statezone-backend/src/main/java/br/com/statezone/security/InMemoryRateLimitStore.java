package br.com.statezone.security;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "rate-limiting.store", havingValue = "memory")
public class InMemoryRateLimitStore implements RateLimitStore {

    private static final int MAX_ATTEMPTS = 5;
    private static final int WINDOW_SECONDS = 60;
    private static final int BLOCKLIST_DURATION_MINUTES = 15;
    private static final int BLOCKLIST_THRESHOLD = 3;

    private final Map<String, SlidingWindow> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blocklist = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    @PostConstruct
    void startCleanup() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            long now = Instant.now().getEpochSecond();
            attempts.entrySet().removeIf(entry -> !entry.getValue().isBlocked());
            blocklist.entrySet().removeIf(entry -> entry.getValue() <= now);
        }, 1, 1, TimeUnit.MINUTES);
    }

    @PreDestroy
    void shutdownCleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    @Override
    public boolean permitir(String chave) {
        Long blockedUntil = blocklist.get(chave);
        if (blockedUntil != null) {
            long now = Instant.now().getEpochSecond();
            if (now < blockedUntil) {
                return false;
            }
            blocklist.remove(chave);
        }

        SlidingWindow window = attempts.computeIfAbsent(chave, k -> new SlidingWindow());
        if (window.isBlocked()) {
            window.incrementBlockCount();
            if (window.getBlockCount() >= BLOCKLIST_THRESHOLD) {
                blocklist.put(chave, Instant.now().getEpochSecond() + BLOCKLIST_DURATION_MINUTES * 60L);
                attempts.remove(chave);
            }
            return false;
        }
        window.record();
        return true;
    }

    private static class SlidingWindow {
        private final long[] timestamps = new long[MAX_ATTEMPTS];
        private int count = 0;
        private int blockCount = 0;

        synchronized boolean isBlocked() {
            if (count < MAX_ATTEMPTS) {
                return false;
            }
            long now = Instant.now().getEpochSecond();
            int oldestIdx = count % MAX_ATTEMPTS;
            return timestamps[oldestIdx] > now - WINDOW_SECONDS;
        }

        synchronized void record() {
            timestamps[count % MAX_ATTEMPTS] = Instant.now().getEpochSecond();
            if (count == Integer.MAX_VALUE) {
                count = 0;
                timestamps[0] = Instant.now().getEpochSecond();
                return;
            }
            count++;
        }

        synchronized void incrementBlockCount() {
            blockCount++;
        }

        synchronized int getBlockCount() {
            return blockCount;
        }
    }
}
