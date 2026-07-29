package br.com.statezone.repository;

import br.com.statezone.model.RefreshToken;
import br.com.statezone.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenId(String tokenId);
    List<RefreshToken> findByUsuario(Usuario usuario);
    List<RefreshToken> findAllByUsuario(Usuario usuario);
    void deleteAllByUsuario(Usuario usuario);
    void deleteAllByExpiryDateBefore(Instant cutoff);
}