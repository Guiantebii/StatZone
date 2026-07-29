package br.com.statezone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
@ConditionalOnProperty(name = "rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitingFilter extends OncePerRequestFilter {

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
            attempts.entrySet().removeIf(entry -> {
                SlidingWindow w = entry.getValue();
                return !w.isBlocked();
            });
            blocklist.entrySet().removeIf(entry -> {
                long blockedSince = entry.getValue();
                return now - blockedSince > BLOCKLIST_DURATION_MINUTES * 60;
            });
        }, 1, 1, TimeUnit.MINUTES);
    }

    @PreDestroy
    void shutdownCleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (request.getMethod().equals("POST") &&
                (path.equals("/api/auth/login") || path.equals("/api/auth/registro"))) {
            String ip = extractClientIp(request);

            Long blockedUntil = blocklist.get(ip);
            if (blockedUntil != null) {
                long now = Instant.now().getEpochSecond();
                if (now < blockedUntil) {
                    writeTooManyRequests(response, "Muitas tentativas. Tente novamente em " + (blockedUntil - now) + " segundos.");
                    return;
                }
                blocklist.remove(ip);
            }

            SlidingWindow window = attempts.computeIfAbsent(ip, k -> new SlidingWindow());

            if (window.isBlocked()) {
                window.incrementBlockCount();
                if (window.getBlockCount() >= BLOCKLIST_THRESHOLD) {
                    blocklist.put(ip, Instant.now().getEpochSecond() + BLOCKLIST_DURATION_MINUTES * 60);
                    attempts.remove(ip);
                    writeTooManyRequests(response, "Conta temporariamente bloqueada por " + BLOCKLIST_DURATION_MINUTES + " minutos devido a múltiplas tentativas.");
                    return;
                }
                writeTooManyRequests(response, "Muitas tentativas. Aguarde 1 minuto.");
                return;
            }

            window.record();
        }

        filterChain.doFilter(request, response);
    }

    private void writeTooManyRequests(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"timestamp\":\"" + Instant.now() + "\",\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + message + "\"}");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] ips = xff.split(",");
            return ips[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private static class SlidingWindow {
        private final long[] timestamps = new long[MAX_ATTEMPTS];
        private int count = 0;
        private int blockCount = 0;

        boolean isBlocked() {
            if (count < MAX_ATTEMPTS) return false;
            long now = Instant.now().getEpochSecond();
            int oldestIdx = count % MAX_ATTEMPTS;
            return timestamps[oldestIdx] > now - WINDOW_SECONDS;
        }

        void record() {
            timestamps[count % MAX_ATTEMPTS] = Instant.now().getEpochSecond();
            if (count == Integer.MAX_VALUE) {
                count = 0;
                timestamps[0] = Instant.now().getEpochSecond();
                return;
            }
            count++;
        }

        void incrementBlockCount() {
            blockCount++;
        }

        int getBlockCount() {
            return blockCount;
        }
    }
}
