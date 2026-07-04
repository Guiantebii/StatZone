package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.eliminatoria.GrupoRequestDto;
import br.com.statezone.dto.eliminatoria.GrupoResponseDto;
import br.com.statezone.dto.time.TimeResumoDto;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.ClassificacaoService;
import br.com.statezone.service.GrupoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GrupoController.class)
@Import(TestSecurityConfig.class)
class GrupoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GrupoService grupoService;

    @MockBean
    private ClassificacaoService classificacaoService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void criarGrupo_deveRetornar201() throws Exception {
        var request = new GrupoRequestDto("Grupo A");
        var response = new GrupoResponseDto(1L, "Grupo A", 10L, "Brasileirão", List.of());

        when(grupoService.criarGrupo(eq(10L), any())).thenReturn(response);

        mockMvc.perform(post("/campeonatos/10/grupos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Grupo A"));
    }

    @Test
    @WithMockUser
    void listarGrupos_deveRetornar200() throws Exception {
        var response = List.of(
                new GrupoResponseDto(1L, "Grupo A", 10L, "Brasileirão", List.of()),
                new GrupoResponseDto(2L, "Grupo B", 10L, "Brasileirão", List.of())
        );

        when(grupoService.listarGrupos(10L)).thenReturn(response);

        mockMvc.perform(get("/campeonatos/10/grupos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void buscarPorId_deveRetornar200() throws Exception {
        var response = new GrupoResponseDto(1L, "Grupo A", 10L, "Brasileirão", List.of());

        when(grupoService.buscarPorId(10L, 1L)).thenReturn(response);

        mockMvc.perform(get("/campeonatos/10/grupos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adicionarTime_deveRetornar200() throws Exception {
        var response = new GrupoResponseDto(1L, "Grupo A", 10L, "Brasileirão",
                List.of(new TimeResumoDto(5L, "Time X", "TIX", null)));

        when(grupoService.adicionarTime(10L, 1L, 5L)).thenReturn(response);

        mockMvc.perform(post("/campeonatos/10/grupos/1/times/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.times.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void gerarFixtures_deveRetornar200() throws Exception {
        mockMvc.perform(post("/campeonatos/10/grupos/1/fixtures"))
                .andExpect(status().isOk());

        verify(grupoService).gerarFixturesPorGrupo(10L, 1L);
    }

    @Test
    @WithMockUser
    void classificacaoDoGrupo_deveRetornar200() throws Exception {
        mockMvc.perform(get("/campeonatos/10/grupos/1/classificacao"))
                .andExpect(status().isOk());

        verify(classificacaoService).gerarClassificacaoPorGrupo(1L);
    }
}
