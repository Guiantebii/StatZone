package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.model.RefreshToken;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RefreshController.class)
@Import(TestSecurityConfig.class)
class RefreshControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    void refresh_semCookie_deveRetornar401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_tokenInvalido_deveRetornar401() throws Exception {
        when(jwtService.extrairEmail("invalid-token")).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh", "invalid-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_tokenExpirado_deveRetornar401() throws Exception {
        var email = "user@test.com";
        var user = org.mockito.Mockito.mock(UserDetails.class);

        when(jwtService.extrairEmail("expired-token")).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(jwtService.isTokenValido("expired-token", user)).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh", "expired-token")))
                .andExpect(status().isUnauthorized());

        verify(jwtService).extrairId("expired-token");
    }

    @Test
    void refresh_tokenSemJti_deveRetornar401() throws Exception {
        var email = "user@test.com";
        var user = org.mockito.Mockito.mock(UserDetails.class);

        when(jwtService.extrairEmail("no-jti-token")).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(jwtService.isTokenValido("no-jti-token", user)).thenReturn(true);
        when(jwtService.isRefreshToken("no-jti-token")).thenReturn(true);
        when(jwtService.extrairId("no-jti-token")).thenReturn(null);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh", "no-jti-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_tokenRevogado_deveRetornar401() throws Exception {
        var email = "user@test.com";
        var user = org.mockito.Mockito.mock(UserDetails.class);
        var stored = RefreshToken.builder().revoked(true).build();

        when(jwtService.extrairEmail("revoked-token")).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(jwtService.isTokenValido("revoked-token", user)).thenReturn(true);
        when(jwtService.isRefreshToken("revoked-token")).thenReturn(true);
        when(jwtService.extrairId("revoked-token")).thenReturn("jti-123");
        when(refreshTokenService.findByTokenId("jti-123")).thenReturn(Optional.of(stored));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh", "revoked-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_valido_deveRetornar200() throws Exception {
        var email = "user@test.com";
        var user = org.mockito.Mockito.mock(UserDetails.class);
        var stored = RefreshToken.builder()
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(jwtService.extrairEmail("valid-token")).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(jwtService.isTokenValido("valid-token", user)).thenReturn(true);
        when(jwtService.isRefreshToken("valid-token")).thenReturn(true);
        when(jwtService.extrairId("valid-token")).thenReturn("jti-456");
        when(refreshTokenService.findByTokenId("jti-456")).thenReturn(Optional.of(stored));
        when(refreshTokenService.rotateRefreshToken("jti-456", user)).thenReturn("new-refresh-token");
        when(jwtService.gerarToken(user)).thenReturn("new-access-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);
        when(jwtService.getRefreshExpirationMs()).thenReturn(1209600000L);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh", "valid-token")))
                .andExpect(status().isOk());
    }
}
