package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.EventoPartidaService;
import br.com.statezone.service.PartidaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartidaController.class)
@Import(TestSecurityConfig.class)
class PartidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private PartidaService partidaService;

    @MockBean
    private EventoPartidaService eventoPartidaService;

    @Test
    @WithMockUser
    void criarPartida_deveRetornar201ComLocation() throws Exception {
        PartidaRequestDto request = new PartidaRequestDto(
                "Morumbi",
                "Árbitro",
                4,
                LocalDateTime.now().plusDays(1),
                StatusPartida.AGENDADA,
                null,
                null,
                1L,
                10L,
                11L
        );
        PartidaResponseDto response = new PartidaResponseDto(
                99L,
                "Morumbi",
                "Árbitro",
                4,
                LocalDateTime.now().plusDays(1),
                StatusPartida.AGENDADA,
                0,
                0,
                1L,
                "Campeonato",
                10L,
                "Mandante",
                11L,
                "Visitante",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(partidaService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/partidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/partidas/99"))
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.status").value("AGENDADA"));

        verify(partidaService).criar(any(PartidaRequestDto.class));
    }

    @Test
    @WithMockUser
    void atualizarPartida_deveUsarIdDaUrlComoPrimeiroParametro() throws Exception {
        PartidaRequestDto request = new PartidaRequestDto(
                "Arena",
                "Árbitro",
                8,
                LocalDateTime.now().plusDays(2),
                StatusPartida.AGENDADA,
                1,
                0,
                1L,
                10L,
                11L
        );
        PartidaResponseDto response = new PartidaResponseDto(
                42L,
                "Arena",
                "Árbitro",
                8,
                LocalDateTime.now().plusDays(2),
                StatusPartida.AGENDADA,
                1,
                0,
                1L,
                "Campeonato",
                10L,
                "Mandante",
                11L,
                "Visitante",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(partidaService.atualizar(eq(42L), any(PartidaRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/partidas/{id}", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("AGENDADA"));

        verify(partidaService).atualizar(eq(42L), any(PartidaRequestDto.class));
    }

    @Test
    @WithMockUser
    void deletarPartida_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/partidas/{id}", 77L))
                .andExpect(status().isNoContent());

        verify(partidaService).deletar(77L);
    }

    @Test
    @WithMockUser
    void adiarPartida_deveRetornar200() throws Exception {
        PartidaResponseDto response = new PartidaResponseDto(
                55L,
                "Arena",
                "Árbitro",
                8,
                LocalDateTime.now().plusDays(3),
                StatusPartida.ADIADA,
                0,
                0,
                1L,
                "Campeonato",
                10L,
                "Mandante",
                11L,
                "Visitante",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(partidaService.adiar(55L)).thenReturn(response);

        mockMvc.perform(patch("/partidas/{id}/adiar", 55L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADIADA"));

        verify(partidaService).adiar(55L);
    }

    @Test
    @WithMockUser
    void cancelarPartida_deveRetornar200() throws Exception {
        PartidaResponseDto response = new PartidaResponseDto(
                56L,
                "Arena",
                "Árbitro",
                8,
                LocalDateTime.now().plusDays(3),
                StatusPartida.CANCELADA,
                0,
                0,
                1L,
                "Campeonato",
                10L,
                "Mandante",
                11L,
                "Visitante",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(partidaService.cancelar(56L)).thenReturn(response);

        mockMvc.perform(patch("/partidas/{id}/cancelar", 56L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        verify(partidaService).cancelar(56L);
    }
}
