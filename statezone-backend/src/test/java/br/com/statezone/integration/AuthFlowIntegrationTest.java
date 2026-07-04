package br.com.statezone.integration;

import br.com.statezone.dto.security.LoginRequest;
import br.com.statezone.enums.Role;
import br.com.statezone.model.Usuario;
import br.com.statezone.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    void login_refresh_logout_flow() throws Exception {
        // create user
        Usuario u = Usuario.builder()
                .email("flow@test.com")
                .senha(passwordEncoder.encode("pass"))
                .role(Role.USER)
                .build();
        usuarioRepository.save(u);

        LoginRequest req = new LoginRequest("flow@test.com", "pass");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().exists("refresh"))
                .andReturn();

        jakarta.servlet.http.Cookie tokenCookie = loginResult.getResponse().getCookie("token");
        jakarta.servlet.http.Cookie refreshCookie = loginResult.getResponse().getCookie("refresh");

        assertNotNull(tokenCookie);
        assertNotNull(refreshCookie);

        mockMvc.perform(get("/api/auth/me").cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("flow@test.com"));

        // call refresh
        MvcResult refreshRes = mockMvc.perform(post("/api/auth/refresh")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().exists("refresh"))
                .andReturn();

        jakarta.servlet.http.Cookie newToken = refreshRes.getResponse().getCookie("token");
        jakarta.servlet.http.Cookie newRefresh = refreshRes.getResponse().getCookie("refresh");

        assertNotNull(newToken);
        assertNotNull(newRefresh);

        // Ensure rotation: token changed
        assertNotEquals(tokenCookie.getValue(), newToken.getValue());

        // logout
        MvcResult logoutRes = mockMvc.perform(post("/api/auth/logout")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .cookie(newToken, newRefresh))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie clearedToken = logoutRes.getResponse().getCookie("token");
        jakarta.servlet.http.Cookie clearedRefresh = logoutRes.getResponse().getCookie("refresh");

        assertNotNull(clearedToken);
        assertNotNull(clearedRefresh);
        assertTrue(clearedToken.getValue().isEmpty() || clearedToken.getMaxAge() == 0);
        assertTrue(clearedRefresh.getValue().isEmpty() || clearedRefresh.getMaxAge() == 0);
    }
}
