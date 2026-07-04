package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.security.LoginRequest;
import br.com.statezone.dto.security.RegistroRequest;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.UnauthorizedException;
import br.com.statezone.repository.UsuarioRepository;
import br.com.statezone.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, properties = {"rate-limiting.enabled=false"})
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private JwtService jwtService;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private br.com.statezone.service.RefreshTokenService refreshTokenService;

    @Test
    void login_deveRetornarToken() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "senha123");
        UserDetails user = new User("user@test.com", "senha123", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.gerarToken(user)).thenReturn("jwt-token-valido");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-valido"));
    }

    @Test
    void login_comCredenciaisInvalidas_deveRetornar401() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "senha_errada");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_comEmailInvalido_deveRetornar400() throws Exception {
        LoginRequest request = new LoginRequest("email-invalido", "senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registro_deveCriarUsuario() throws Exception {
        RegistroRequest request = new RegistroRequest("novo@test.com", "senha12345");

        when(usuarioRepository.existsByEmail("novo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("encoded");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(usuarioRepository).save(any());
    }

    @Test
    void registro_comEmailJaExistente_deveRetornar409() throws Exception {
        RegistroRequest request = new RegistroRequest("existente@test.com", "senha12345");

        when(usuarioRepository.existsByEmail("existente@test.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void registro_comSenhaCurta_deveRetornar400() throws Exception {
        RegistroRequest request = new RegistroRequest("user@test.com", "curta");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_deveRetornarDadosDoUsuario() throws Exception {
        UserDetails user = new User("admin@test.com", "senha",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("admin@test.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void logout_deveLimparCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}
