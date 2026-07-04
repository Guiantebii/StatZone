package br.com.statezone.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @BeforeEach
    void setUp() {
        SecretKey key = Keys.hmacShaKeyFor("esta-e-uma-chave-secreta-muito-longa-para-teste-12345678".getBytes());
        String base64Secret = Encoders.BASE64.encode(key.getEncoded());
        ReflectionTestUtils.setField(jwtService, "secret", base64Secret);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    private UserDetails createUser(String email) {
        return new User(email, "senha", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void gerarToken_deveCriarTokenValido() {
        UserDetails user = createUser("test@test.com");
        String token = jwtService.gerarToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairEmail(token)).isEqualTo("test@test.com");
    }

    @Test
    void extrairEmail_deveRetornarEmailCorreto() {
        UserDetails user = createUser("admin@test.com");
        String token = jwtService.gerarToken(user);

        String email = jwtService.extrairEmail(token);
        assertThat(email).isEqualTo("admin@test.com");
    }

    @Test
    void isTokenValido_deveRetornarTrueParaTokenValido() {
        UserDetails user = createUser("user@test.com");
        String token = jwtService.gerarToken(user);

        assertThat(jwtService.isTokenValido(token, user)).isTrue();
    }

    @Test
    void isTokenValido_deveRetornarFalseParaEmailDiferente() {
        UserDetails user = createUser("user@test.com");
        UserDetails otherUser = createUser("other@test.com");
        String token = jwtService.gerarToken(user);

        assertThat(jwtService.isTokenValido(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValido_deveRetornarFalseParaTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        UserDetails user = createUser("user@test.com");
        String token = jwtService.gerarToken(user);

        assertThat(jwtService.isTokenValido(token, user)).isFalse();
    }

    @Test
    void extrairEmail_deveLancarExcecaoParaTokenInvalido() {
        assertThrows(Exception.class, () ->
                jwtService.extrairEmail("token.invalido.aqui")
        );
    }

    @Test
    void getExpirationMs_deveRetornarValorConfigurado() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(3600000L);
    }
}
