package br.com.statezone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private final RateLimitingFilter filter = new RateLimitingFilter();
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void loginComMenosDe5Tentativas_devePassar() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");

        for (int i = 0; i < 4; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        assertThat(response.getStatus()).isNotEqualTo(429);
        verify(filterChain, times(4)).doFilter(request, response);
    }

    @Test
    void loginCom5Tentativas_deveBloquear() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");

        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
            response = new MockHttpServletResponse();
        }

        filter.doFilterInternal(request, response, filterChain);
        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void registroTambem_deveSerRateLimited() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/registro");

        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
            response = new MockHttpServletResponse();
        }

        filter.doFilterInternal(request, response, filterChain);
        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void outrasRotasNaoDevemSerAfetadas() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/times");

        for (int i = 0; i < 100; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        assertThat(response.getStatus()).isNotEqualTo(429);
    }

    @Test
    void respostaDeBloqueioDeveSerJson() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");

        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
            response = new MockHttpServletResponse();
        }

        filter.doFilterInternal(request, response, filterChain);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getContentAsString()).contains("Muitas tentativas");
    }
}
