package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.eventoPartida.EventoPartidaRequestDto;
import br.com.statezone.dto.eventoPartida.EventoPartidaResponseDto;
import br.com.statezone.dto.eventoPartida.EventoTimelineResponseDto;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.EventoPartidaService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventoPartidaController.class)
@Import(TestSecurityConfig.class)
class EventoPartidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventoPartidaService eventoPartidaService;

    @Test
    @WithMockUser
    void registrarEvento_deveRetornar201ComLocation() throws Exception {
        EventoPartidaRequestDto request = new EventoPartidaRequestDto(
                TipoEvento.GOL,
                12,
                null,
                "Gol",
                20L,
                21L,
                null
        );
        EventoPartidaResponseDto response = new EventoPartidaResponseDto(
                50L,
                TipoEvento.GOL,
                12,
                null,
                "Gol",
                null,
                false,
                7L,
                20L,
                "Atacante",
                "Time A",
                21L,
                "Assistente"
        );

        when(eventoPartidaService.registrarEvento(any(EventoPartidaRequestDto.class), eq(7L))).thenReturn(response);

        mockMvc.perform(post("/partidas/{partidaId}/eventos", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/partidas/7/eventos/50"))
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.tipoEvento").value("GOL"))
                .andExpect(jsonPath("$.partidaId").value(7));

        verify(eventoPartidaService).registrarEvento(any(EventoPartidaRequestDto.class), eq(7L));
    }

    @Test
    @WithMockUser
    void listarEventos_deveRetornarLista() throws Exception {
        EventoPartidaResponseDto response = new EventoPartidaResponseDto(
                51L,
                TipoEvento.CARTAO_AMARELO,
                33,
                null,
                "Amarelo",
                null,
                false,
                7L,
                20L,
                "Atacante",
                "Time A",
                null,
                null
        );

        when(eventoPartidaService.listarEventosPorPartida(7L)).thenReturn(List.of(response));

        mockMvc.perform(get("/partidas/{partidaId}/eventos", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(51))
                .andExpect(jsonPath("$[0].tipoEvento").value("CARTAO_AMARELO"));

        verify(eventoPartidaService).listarEventosPorPartida(7L);
    }

    @Test
    @WithMockUser
    void timeline_deveRetornarListaDeEventos() throws Exception {
        EventoTimelineResponseDto response = new EventoTimelineResponseDto(
                52L,
                TipoEvento.GOL,
                45,
                1,
                "45+1'",
                10L,
                "Time A",
                20L,
                "Jogador",
                21L,
                "Assistente"
        );

        when(eventoPartidaService.buscarTimeline(7L)).thenReturn(List.of(response));

        mockMvc.perform(get("/partidas/{partidaId}/timeline", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tempo").value("45+1'"))
                .andExpect(jsonPath("$[0].tipo").value("GOL"));

        verify(eventoPartidaService).buscarTimeline(7L);
    }
}
