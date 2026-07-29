package br.com.statezone.service;

import br.com.statezone.model.RefreshToken;
import br.com.statezone.model.Usuario;
import br.com.statezone.repository.RefreshTokenRepository;
import br.com.statezone.repository.UsuarioRepository;
import br.com.statezone.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

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
        refreshTokenRepository.findByUsuario(usuario).forEach(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public String rotateRefreshToken(String oldTokenId, UserDetails userDetails) {
        Optional<RefreshToken> oldToken = refreshTokenRepository.findByTokenId(oldTokenId);

        if (oldToken.isPresent()) {
            RefreshToken token = oldToken.get();

            if (token.isRevoked()) {
                log.warn("Tentativa de rotação de refresh token já revogado (roubo detectado) para usuário: {}",
                        userDetails.getUsername());
                revokeAllForUsuario(token.getUsuario());
                throw new IllegalStateException("Refresh token reuse detected. All tokens revoked for security.");
            }

            token.setRevoked(true);
            refreshTokenRepository.save(token);
        }

        return createRefreshToken(userDetails);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpired() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }
}
