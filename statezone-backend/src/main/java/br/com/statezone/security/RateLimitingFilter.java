package br.com.statezone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@Order(1)
@ConditionalOnProperty(name = "rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitStore store;
    private final boolean trustProxy;

    public RateLimitingFilter(RateLimitStore store, boolean trustProxy) {
        this.store = store;
        this.trustProxy = trustProxy;
    }

    @Autowired
    public RateLimitingFilter(ObjectProvider<RateLimitStore> stores,
                              @Value("${rate-limiting.trust-proxy:false}") boolean trustProxy) {
        this(stores.getIfAvailable(InMemoryRateLimitStore::new), trustProxy);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("POST".equals(request.getMethod())
                && ("/api/auth/login".equals(path) || "/api/auth/registro".equals(path))
                && !store.permitir(extractClientIp(request))) {
            writeTooManyRequests(response, "Muitas tentativas. Aguarde antes de tentar de novo.");
            return;
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
        if (!trustProxy) {
            return request.getRemoteAddr();
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
