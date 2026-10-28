package br.com.statezone.repository;

import br.com.statezone.model.RefreshToken;
import br.com.statezone.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenId(String tokenId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshToken r WHERE r.tokenId = :tokenId")
    Optional<RefreshToken> findByTokenIdForUpdate(@Param("tokenId") String tokenId);
    List<RefreshToken> findByUsuario(Usuario usuario);
    List<RefreshToken> findAllByUsuario(Usuario usuario);
    void deleteAllByUsuario(Usuario usuario);
    void deleteAllByExpiryDateBefore(Instant cutoff);
}