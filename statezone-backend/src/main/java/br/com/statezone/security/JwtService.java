package br.com.statezone.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms:1209600000}")
    private long refreshExpirationMs; // default 14 days

    public long getExpirationMs() {
        return expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String gerarToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setId(java.util.UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String gerarRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        // include jti so refresh tokens can be tracked/revoked server-side
        String jti = java.util.UUID.randomUUID().toString();

        return Jwts.builder()
                .setId(jti)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .setExpiration(expiry)
                .claim("type", "refresh")
                .signWith(getSigningKey())
                .compact();
    }

    public String extrairId(String token) {
        try {
            return getClaims(token).getId();
        } catch (Exception e) {
            log.warn("Falha ao extrair jti do token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Object t = getClaims(token).get("type");
            return "refresh".equals(String.valueOf(t));
        } catch (Exception e) {
            return false;
        }
    }

    public String extrairEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValido(String token, UserDetails userDetails) {
        try {
            String email = extrairEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpirado(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado para usuário: {}", userDetails.getUsername());
            return false;
        } catch (SignatureException e) {
            log.error("Possível ataque: Assinatura JWT inválida ou manipulada");
            return false;
        } catch (JwtException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Erro ao validar token", e);
            return false;
        }
    }

    private boolean isTokenExpirado(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}