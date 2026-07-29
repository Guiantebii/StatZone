package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.campeonato.CampeonatoRequestDto;
import br.com.statezone.dto.campeonato.CampeonatoResponseDto;
import br.com.statezone.dto.classificacao.ClassificacaoResponseDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.integration.apifootball.ApiFootballClient;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.CampeonatoService;
import br.com.statezone.service.ClassificacaoService;
import br.com.statezone.service.EstatisticasJogadorService;
import br.com.statezone.service.FixtureGeneratorService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CampeonatoController.class)
@Import(TestSecurityConfig.class)
class CampeonatoControllerTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CampeonatoService campeonatoService;

    @MockBean
    private ClassificacaoService classificacaoService;

    @MockBean
    private FixtureGeneratorService fixtureGeneratorService;

    @MockBean
    private EstatisticasJogadorService estatisticasJogadorService;

    @MockBean
    private ApiFootballClient apiFootballClient;

    @Test
    @WithMockUser
    void criarCampeonato_deveRetornar201ComLocation() throws Exception {
        CampeonatoRequestDto request = new CampeonatoRequestDto(
                "Liga", "Brasil", "2026", "https://example.com/logo.png", "PONTOS_CORRIDOS", 3
        );
        CampeonatoResponseDto response = new CampeonatoResponseDto(
                1L, "Liga", "Brasil", "2026", "https://example.com/logo.png", "PONTOS_CORRIDOS", 3,
                "RASCUNHO", LocalDateTime.now(), LocalDateTime.now(), List.of()
        );

        when(campeonatoService.criarCampeonato(any())).thenReturn(response);

        mockMvc.perform(post("/campeonatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/campeonatos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Liga"));

        verify(campeonatoService).criarCampeonato(any(CampeonatoRequestDto.class));
    }

    @Test
    @WithMockUser
    void listarCampeonatos_deveRetornarLista() throws Exception {
        CampeonatoResponseDto response = new CampeonatoResponseDto(
                1L, "Liga", "Brasil", "2026", "logo.png", "PONTOS_CORRIDOS", 3,
                "RASCUNHO", LocalDateTime.now(), LocalDateTime.now(), List.of(10L)
        );
        when(campeonatoService.listarTodosCampeonatos()).thenReturn(List.of(response));

        mockMvc.perform(get("/campeonatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].timesIds[0]").value(10));

        verify(campeonatoService).listarTodosCampeonatos();
    }

    @Test
    @WithMockUser
    void adicionarTime_deveRetornar200() throws Exception {
        mockMvc.perform(post("/campeonatos/{campeonatoId}/times/{timeId}", 1L, 10L))
                .andExpect(status().isOk());

        verify(campeonatoService).adicionarTime(1L, 10L);
    }

    @Test
    @WithMockUser
    void gerarClassificacao_semTurno_deveRetornarLista() throws Exception {
        ClassificacaoResponseDto response = new ClassificacaoResponseDto(10L, "Time", 6, 2, 2, 0, 0, 4, 1, 3, 1, 100.0);
        when(classificacaoService.gerarClassificacao(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/campeonatos/{campeonatoId}/classificacao", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].timeId").value(10))
                .andExpect(jsonPath("$[0].pontos").value(6));

        verify(classificacaoService).gerarClassificacao(1L);
    }

    @Test
    @WithMockUser
    void gerarClassificacao_comTurno_deveUsarMetodoEspecifico() throws Exception {
        ClassificacaoResponseDto response = new ClassificacaoResponseDto(10L, "Time", 6, 2, 2, 0, 0, 4, 1, 3, 1, 100.0);
        when(classificacaoService.gerarClassificacaoPorTurno(1L, 1)).thenReturn(List.of(response));

        mockMvc.perform(get("/campeonatos/{campeonatoId}/classificacao", 1L).param("turno", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].timeId").value(10));

        verify(classificacaoService).gerarClassificacaoPorTurno(1L, 1);
    }

    @Test
    @WithMockUser
    void gerarFixtures_deveRetornar201ComLocation() throws Exception {
        PartidaResponseDto response = new PartidaResponseDto(
                100L,
                "Arena",
                "Arbitro",
                1,
                LocalDateTime.now().plusDays(1),
                StatusPartida.AGENDADA,
                0,
                0,
                1L,
                "Liga",
                10L,
                "Mandante",
                11L,
                "Visitante",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(fixtureGeneratorService.gerarPartida(1L)).thenReturn(List.of(response));

        mockMvc.perform(post("/campeonatos/{id}/fixtures", 1L))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/campeonatos/1/partidas"))
                .andExpect(jsonPath("$[0].id").value(100));

        verify(fixtureGeneratorService).gerarPartida(1L);
    }

    @Test
    @WithMockUser
    void atualizarEExcluirCampeonato_devemChamarService() throws Exception {
        CampeonatoRequestDto request = new CampeonatoRequestDto(
                "Liga 2", "Brasil", "2027", "https://example.com/logo2.png", "MATA_MATA", 4
        );
        CampeonatoResponseDto response = new CampeonatoResponseDto(
                1L, "Liga 2", "Brasil", "2027", "https://example.com/logo2.png", "MATA_MATA", 4,
                "RASCUNHO", LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(campeonatoService.atualizarCampeonato(eq(request), eq(1L))).thenReturn(response);

        mockMvc.perform(put("/campeonatos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Liga 2"));

        mockMvc.perform(delete("/campeonatos/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(campeonatoService).atualizarCampeonato(eq(request), eq(1L));
        verify(campeonatoService).deletarCampeonato(1L);
    }
}