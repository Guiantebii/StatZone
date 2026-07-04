package br.com.statezone.service;

import br.com.statezone.model.RefreshToken;
import br.com.statezone.model.Usuario;
import br.com.statezone.repository.RefreshTokenRepository;
import br.com.statezone.repository.UsuarioRepository;
import br.com.statezone.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Test
    void createRefreshToken_persistsTokenWithJti() {
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        JwtService jwtService = mock(JwtService.class);

        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, usuarioRepository, jwtService);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user@test.com").password("x").roles("USER").build();

        Usuario usuario = Usuario.builder().id(10L).email("user@test.com").build();
        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        when(jwtService.gerarRefreshToken(userDetails)).thenReturn("refresh-token-value");
        when(jwtService.extrairId("refresh-token-value")).thenReturn("jti-123");
        when(jwtService.getRefreshExpirationMs()).thenReturn(1209600000L);

        String result = service.createRefreshToken(userDetails);

        assertThat(result).isEqualTo("refresh-token-value");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getTokenId()).isEqualTo("jti-123");
        assertThat(saved.getUsuario().getId()).isEqualTo(10L);
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getExpiryDate()).isAfter(Instant.now());
    }
}
