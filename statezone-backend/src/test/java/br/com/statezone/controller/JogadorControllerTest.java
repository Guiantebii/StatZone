package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.estatisticasJogador.EstatisticasJogadorResponseDto;
import br.com.statezone.dto.jogador.JogadorRequestDto;
import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.enums.PeForte;
import br.com.statezone.enums.Posicao;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.EstatisticasJogadorService;
import br.com.statezone.service.JogadorService;
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

import java.math.BigDecimal;
import java.time.LocalDate;

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

@WebMvcTest(JogadorController.class)
@Import(TestSecurityConfig.class)
class JogadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JogadorService jogadorService;

    @MockBean
    private EstatisticasJogadorService estatisticasJogadorService;

    @Test
    @WithMockUser
    void criarJogador_deveRetornar201ComLocation() throws Exception {
        JogadorRequestDto request = new JogadorRequestDto(
                "Jogador",
                LocalDate.now().minusYears(25),
                "Brasil",
                Posicao.CENTROAVANTE,
                9,
                new BigDecimal("1.84"),
                new BigDecimal("78.0"),
                new BigDecimal("5000000"),
                PeForte.DIREITO,
                "https://example.com/foto.png",
                1L
        );
        JogadorResponseDto response = new JogadorResponseDto(
                10L,
                "Jogador",
                LocalDate.now().minusYears(25),
                "Brasil",
                Posicao.CENTROAVANTE,
                9,
                new BigDecimal("1.84"),
                new BigDecimal("78.0"),
                new BigDecimal("5000000"),
                PeForte.DIREITO,
                "https://example.com/foto.png",
                1L,
                "Time"
        );

        when(jogadorService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/jogadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/jogadores/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Jogador"));

        verify(jogadorService).criar(any(JogadorRequestDto.class));
    }

    @Test
    @WithMockUser
    void listarJogadores_deveRetornarLista() throws Exception {
        JogadorResponseDto response = new JogadorResponseDto(
                10L,
                "Jogador",
                LocalDate.now().minusYears(25),
                "Brasil",
                Posicao.CENTROAVANTE,
                9,
                new BigDecimal("1.84"),
                new BigDecimal("78.0"),
                new BigDecimal("5000000"),
                PeForte.DIREITO,
                "https://example.com/foto.png",
                1L,
                "Time"
        );

        when(jogadorService.listarTodosJogadores()).thenReturn(java.util.List.of(response));

        mockMvc.perform(get("/jogadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nomeTime").value("Time"));

        verify(jogadorService).listarTodosJogadores();
    }

    @Test
    @WithMockUser
    void obterJogadorPorId_deveRetornarItem() throws Exception {
        JogadorResponseDto response = new JogadorResponseDto(
                10L,
                "Jogador",
                LocalDate.now().minusYears(25),
                "Brasil",
                Posicao.CENTROAVANTE,
                9,
                new BigDecimal("1.84"),
                new BigDecimal("78.0"),
                new BigDecimal("5000000"),
                PeForte.DIREITO,
                "https://example.com/foto.png",
                1L,
                "Time"
        );

        when(jogadorService.obterJogadorPorId(10L)).thenReturn(response);

        mockMvc.perform(get("/jogadores/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(jogadorService).obterJogadorPorId(10L);
    }

    @Test
    @WithMockUser
    void atualizarJogador_deveUsarIdDaUrl() throws Exception {
        JogadorRequestDto request = new JogadorRequestDto(
                "Jogador",
                LocalDate.now().minusYears(26),
                "Brasil",
                Posicao.MEIO_CAMPO,
                8,
                new BigDecimal("1.80"),
                new BigDecimal("76.0"),
                new BigDecimal("4000000"),
                PeForte.ESQUERDO,
                "https://example.com/foto2.png",
                1L
        );
        JogadorResponseDto response = new JogadorResponseDto(
                10L,
                "Jogador",
                LocalDate.now().minusYears(26),
                "Brasil",
                Posicao.MEIO_CAMPO,
                8,
                new BigDecimal("1.80"),
                new BigDecimal("76.0"),
                new BigDecimal("4000000"),
                PeForte.ESQUERDO,
                "https://example.com/foto2.png",
                1L,
                "Time"
        );

        when(jogadorService.atualizarJogador(eq(request), eq(10L))).thenReturn(response);

        mockMvc.perform(put("/jogadores/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posicao").value("MEIO_CAMPO"));

        verify(jogadorService).atualizarJogador(eq(request), eq(10L));
    }

    @Test
    @WithMockUser
    void deletarJogador_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/jogadores/{id}", 10L))
                .andExpect(status().isNoContent());

        verify(jogadorService).deletarJogador(10L);
    }

    @Test
    @WithMockUser
    void estatisticas_deveRetornarResumo() throws Exception {
        EstatisticasJogadorResponseDto response = new EstatisticasJogadorResponseDto(
                10L,
                "Jogador",
                "Time",
                3,
                2,
                10,
                1,
                0,
                4,
                8,
                0,
                0,
                1,
                2,
                1.5,
                1.0,
                0.0
        );

        when(estatisticasJogadorService.buscarPorJogador(10L)).thenReturn(response);

        mockMvc.perform(get("/jogadores/{id}/estatisticas", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jogadorId").value(10))
                .andExpect(jsonPath("$.gols").value(3));

        verify(estatisticasJogadorService).buscarPorJogador(10L);
    }
}
