package br.com.statezone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final int WINDOW_SECONDS = 60;

    private final Map<String, SlidingWindow> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (request.getMethod().equals("POST") && path.equals("/api/auth/login")) {
            String ip = request.getRemoteAddr();
            SlidingWindow window = attempts.computeIfAbsent(ip, k -> new SlidingWindow());

            if (window.isBlocked()) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"timestamp\":\"" + Instant.now() + "\",\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Muitas tentativas. Aguarde 1 minuto.\"}");
                return;
            }

            window.record();
        }

        filterChain.doFilter(request, response);
    }

    private static class SlidingWindow {
        private final long[] timestamps = new long[MAX_ATTEMPTS];
        private int count = 0;

        boolean isBlocked() {
            if (count < MAX_ATTEMPTS) return false;
            long now = Instant.now().getEpochSecond();
            int oldestIdx = count % MAX_ATTEMPTS;
            return timestamps[oldestIdx] > now - WINDOW_SECONDS;
        }

        void record() {
            timestamps[count % MAX_ATTEMPTS] = Instant.now().getEpochSecond();
            count++;
        }
    }
}
