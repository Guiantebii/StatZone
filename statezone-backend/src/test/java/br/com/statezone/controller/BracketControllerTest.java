package br.com.statezone.controller;

import br.com.statezone.dto.eliminatoria.ConfrontoEliminatorioResponseDto;
import br.com.statezone.dto.eliminatoria.FaseEliminatoriaRequestDto;
import br.com.statezone.dto.eliminatoria.FaseEliminatoriaResponseDto;
import br.com.statezone.dto.time.TimeResumoDto;
import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.service.BracketService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(BracketController.class)
class BracketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BracketService bracketService;

    @Test
    void criarFase_deveRetornar201() throws Exception {
        FaseEliminatoriaRequestDto request = new FaseEliminatoriaRequestDto(FaseEnum.OITAVAS, true);
        FaseEliminatoriaResponseDto response = new FaseEliminatoriaResponseDto(1L, 10L, FaseEnum.OITAVAS, true, List.of());

        when(bracketService.criarFase(eq(10L), any(FaseEliminatoriaRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/campeonatos/{campeonatoId}/fases", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fase").value("OITAVAS"));

        verify(bracketService).criarFase(eq(10L), any(FaseEliminatoriaRequestDto.class));
    }

    @Test
    void listarFases_deveRetornarLista() throws Exception {
        FaseEliminatoriaResponseDto response = new FaseEliminatoriaResponseDto(1L, 10L, FaseEnum.OITAVAS, true, List.of());
        when(bracketService.listarFases(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/campeonatos/{campeonatoId}/fases", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bracketService).listarFases(10L);
    }

    @Test
    void gerarPrimeiraFase_deveRetornar200() throws Exception {
        mockMvc.perform(post("/campeonatos/{campeonatoId}/fases/{faseId}/gerar", 10L, 20L)
                        .param("vagasPorGrupo", "2"))
                .andExpect(status().isOk());

        verify(bracketService).gerarPrimeiraFase(10L, 20L, 2);
    }

    @Test
    void listarConfrontos_deveRetornarLista() throws Exception {
        ConfrontoEliminatorioResponseDto response = new ConfrontoEliminatorioResponseDto(
                1L,
                new TimeResumoDto(10L, "Time A", "TIM", "escudo-a.png"),
                new TimeResumoDto(11L, "Time B", "TIM", "escudo-b.png"),
                100L,
                null,
                null,
                StatusConfronto.PENDENTE
        );
        when(bracketService.listarConfrontos(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/campeonatos/{campeonatoId}/confrontos", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].statusConfronto").value("PENDENTE"));

        verify(bracketService).listarConfrontos(10L);
    }

    @Test
    void encerrarConfronto_deveRetornarFaseAtualizada() throws Exception {
        FaseEliminatoriaResponseDto response = new FaseEliminatoriaResponseDto(1L, 10L, FaseEnum.OITAVAS, true, List.of());
        when(bracketService.encerraConfronto(10L, 100L)).thenReturn(response);

        mockMvc.perform(post("/campeonatos/{campeonatoId}/confrontos/{confrontoId}/encerrar", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bracketService).encerraConfronto(10L, 100L);
    }
}
