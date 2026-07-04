package br.com.statezone.service;

import br.com.statezone.model.RefreshToken;
import br.com.statezone.model.Usuario;
import br.com.statezone.repository.RefreshTokenRepository;
import br.com.statezone.repository.UsuarioRepository;
import br.com.statezone.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    // Create and persist a refresh token; return the token string
    public String createRefreshToken(UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado ao criar refresh token"));

        String refresh = jwtService.gerarRefreshToken(userDetails);
        String jti = jwtService.extrairId(refresh);

        RefreshToken token = RefreshToken.builder()
                .tokenId(jti)
                .usuario(usuario)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(token);
        return refresh;
    }

    public Optional<RefreshToken> findByTokenId(String tokenId) {
        return refreshTokenRepository.findByTokenId(tokenId);
    }

    public void revokeByTokenId(String tokenId) {
        refreshTokenRepository.findByTokenId(tokenId).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void revokeAllForUsuario(Usuario usuario) {
        refreshTokenRepository.deleteAllByUsuario(usuario);
    }

    // Rotate: remove old token record (if any) and persist a new refresh token, returning its token string
    @Transactional
    public String rotateRefreshToken(String oldTokenId, UserDetails userDetails) {
        // remove old
        refreshTokenRepository.findByTokenId(oldTokenId).ifPresent(refreshTokenRepository::delete);

        // create new
        return createRefreshToken(userDetails);
    }

    // cleanup expired tokens
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 3 * * *") // daily at 03:00
    public void cleanupExpired() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }
}