package br.com.statezone.controller;

import br.com.statezone.dto.suspensao.SuspensaoResponseDto;
import br.com.statezone.enums.MotivoSuspensao;
import br.com.statezone.service.SuspensaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuspensaoController.class)
class SuspensaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuspensaoService suspensaoService;

    @Test
    void listarSuspensoesProximaRodada_deveRetornarLista() throws Exception {
        SuspensaoResponseDto response = new SuspensaoResponseDto(
                20L,
                "Jogador",
                "foto.png",
                "Time",
                "escudo.png",
                12,
                MotivoSuspensao.ACUMULO_AMARELOS
        );

        when(suspensaoService.listarSuspensoesProximaRodada(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/campeonatos/{id}/suspensoes", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jogadorId").value(20))
                .andExpect(jsonPath("$[0].motivo").value("ACUMULO_AMARELOS"));

        verify(suspensaoService).listarSuspensoesProximaRodada(1L);
    }

    @Test
    void listarSuspensoesPorRodada_deveRetornarListaDaRodada() throws Exception {
        SuspensaoResponseDto response = new SuspensaoResponseDto(
                21L,
                "Jogador 2",
                "foto2.png",
                "Time",
                "escudo.png",
                15,
                MotivoSuspensao.CARTAO_VERMELHO
        );

        when(suspensaoService.listarSuspensoesPorRodada(1L, 15)).thenReturn(List.of(response));

        mockMvc.perform(get("/campeonatos/{id}/suspensoes/rodada/{rodada}", 1L, 15))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jogadorId").value(21))
                .andExpect(jsonPath("$[0].rodadaSuspensao").value(15))
                .andExpect(jsonPath("$[0].motivo").value("CARTAO_VERMELHO"));

        verify(suspensaoService).listarSuspensoesPorRodada(1L, 15);
    }
}
