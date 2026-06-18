package br.com.statezone.controller;

import br.com.statezone.dto.escalacao.EscalacaoPartidaListResponseDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaRequestDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaResponseDto;
import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.Posicao;
import br.com.statezone.service.EscalacaoPartidaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

@WebMvcTest(EscalacaoPartidaController.class)
class EscalacaoPartidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EscalacaoPartidaService escalacaoPartidaService;

    @Test
    void adicionarJogador_deveRetornar201() throws Exception {
        EscalacaoPartidaRequestDto request = new EscalacaoPartidaRequestDto(20L, FuncaoEscalacao.TITULAR, Posicao.CENTROAVANTE, 9);
        EscalacaoPartidaResponseDto response = new EscalacaoPartidaResponseDto(1L, 20L, "Jogador", "foto.png", "Time", "escudo.png", FuncaoEscalacao.TITULAR, Posicao.CENTROAVANTE, 9, true);

        when(escalacaoPartidaService.adicionarJogador(eq(30L), any(EscalacaoPartidaRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/partidas/{id}/escalacao", 30L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.funcao").value("TITULAR"));

        verify(escalacaoPartidaService).adicionarJogador(eq(30L), any(EscalacaoPartidaRequestDto.class));
    }

    @Test
    void buscarEscalacao_deveRetornarTitularesEReservas() throws Exception {
        EscalacaoPartidaResponseDto titular = new EscalacaoPartidaResponseDto(1L, 20L, "Titular", "foto.png", "Time", "escudo.png", FuncaoEscalacao.TITULAR, Posicao.CENTROAVANTE, 9, true);
        EscalacaoPartidaResponseDto reserva = new EscalacaoPartidaResponseDto(2L, 21L, "Reserva", "foto2.png", "Time", "escudo.png", FuncaoEscalacao.RESERVA, Posicao.MEIO_CAMPO, 18, true);
        EscalacaoPartidaListResponseDto response = new EscalacaoPartidaListResponseDto(30L, List.of(titular), List.of(reserva));

        when(escalacaoPartidaService.buscarEscalacao(30L)).thenReturn(response);

        mockMvc.perform(get("/partidas/{id}/escalacao", 30L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partidaId").value(30))
                .andExpect(jsonPath("$.titulares[0].jogadorId").value(20))
                .andExpect(jsonPath("$.reservas[0].funcao").value("RESERVA"));

        verify(escalacaoPartidaService).buscarEscalacao(30L);
    }
}
