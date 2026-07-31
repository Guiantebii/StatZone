package br.com.statezone.controller;

import br.com.statezone.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class RefreshController {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final br.com.statezone.service.RefreshTokenService refreshTokenService;

    private final boolean cookieSecure;

    public RefreshController(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            br.com.statezone.service.RefreshTokenService refreshTokenService,
            @Value("${app.security.cookie-secure:true}") boolean cookieSecure) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) return ResponseEntity.status(401).build();

        String refresh = null;
        for (jakarta.servlet.http.Cookie c : cookies) {
            if ("refresh".equals(c.getName())) {
                refresh = c.getValue();
                break;
            }
        }

        if (refresh == null) return ResponseEntity.status(401).build();

        String email;
        try {
            email = jwtService.extrairEmail(refresh);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }

        UserDetails user = userDetailsService.loadUserByUsername(email);

        // confirm token integrity, expiry and that it's a refresh token
        if (!jwtService.isTokenValido(refresh, user) || !jwtService.isRefreshToken(refresh)) {
            // revoke server-side record if present
            String jti = jwtService.extrairId(refresh);
            if (jti != null) refreshTokenService.revokeByTokenId(jti);
            return ResponseEntity.status(401).build();
        }

        // verify server-side record exists and is not revoked
        String oldJti = jwtService.extrairId(refresh);
        if (oldJti == null) return ResponseEntity.status(401).build();

        var stored = refreshTokenService.findByTokenId(oldJti);
        if (stored.isEmpty() || stored.get().isRevoked()) {
            return ResponseEntity.status(401).build();
        }

        // rotate: revoke old token and create new one (with reuse detection)
        String newRefresh;
        try {
            newRefresh = refreshTokenService.rotateRefreshToken(oldJti, user);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).build();
        }

        String newAccess = jwtService.gerarToken(user);
        ResponseCookie tokenCookie = ResponseCookie.from("token", newAccess)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("None")
                .path("/")
                .maxAge(jwtService.getExpirationMs() / 1000)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh", newRefresh)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("None")
                .path("/api/auth/refresh")
                .maxAge(jwtService.getRefreshExpirationMs() / 1000)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok().build();
    }
}
