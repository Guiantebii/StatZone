package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.time.TimeRequestDto;
import br.com.statezone.dto.time.TimeResponseDto;
import br.com.statezone.model.TipoTime;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.ApiFootballImportService;
import br.com.statezone.service.TimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TimeController.class, properties = {"rate-limiting.enabled=false"})
@Import(TestSecurityConfig.class)
class TimeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private TimeService timeService;
    @MockBean private ApiFootballImportService apiFootballImportService;

    @Test
    @WithMockUser
    void criarTime_deveRetornar201() throws Exception {
        TimeRequestDto request = new TimeRequestDto(
                "Flamengo", "FLA", TipoTime.CLUBE, "Rio de Janeiro",
                "Brasil", "https://example.com/logo.png", "Filipe Luís",
                "Maracanã", LocalDate.of(2020, 1, 1)
        );
        TimeResponseDto response = new TimeResponseDto(
                1L, "Flamengo", "FLA", TipoTime.CLUBE, "Rio de Janeiro",
                "Brasil", "https://example.com/logo.png", "Filipe Luís",
                "Maracanã", LocalDate.of(2020, 1, 1),
                null, null
        );

        when(timeService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/times/1"))
                .andExpect(jsonPath("$.nome").value("Flamengo"))
                .andExpect(jsonPath("$.sigla").value("FLA"));
    }

    @Test
    @WithMockUser
    void listarTimes_deveRetornarLista() throws Exception {
        TimeResponseDto t1 = new TimeResponseDto(1L, "Flamengo", "FLA", TipoTime.CLUBE,
                "Rio de Janeiro", "Brasil", "https://example.com/fla.png", "Filipe Luís",
                "Maracanã", LocalDate.of(2020, 1, 1), null, null);
        TimeResponseDto t2 = new TimeResponseDto(2L, "Palmeiras", "PAL", TipoTime.CLUBE,
                "São Paulo", "Brasil", "https://example.com/pal.png", "Abel Ferreira",
                "Allianz", LocalDate.of(2020, 1, 1), null, null);

        when(timeService.listarTodosTimes(any(Pageable.class))).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Flamengo"))
                .andExpect(jsonPath("$[1].nome").value("Palmeiras"));
    }

    @Test
    @WithMockUser
    void buscarTimePorId_deveRetornarTime() throws Exception {
        TimeResponseDto response = new TimeResponseDto(1L, "Flamengo", "FLA", TipoTime.CLUBE,
                "Rio de Janeiro", "Brasil", "https://example.com/fla.png", "Filipe Luís",
                "Maracanã", LocalDate.of(2020, 1, 1), null, null);

        when(timeService.obterTimePorId(1L)).thenReturn(response);

        mockMvc.perform(get("/times/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Flamengo"));
    }

    @Test
    @WithMockUser
    void atualizarTime_deveRetornar200() throws Exception {
        TimeRequestDto request = new TimeRequestDto(
                "Flamengo Updated", "FLA", TipoTime.CLUBE, "Rio de Janeiro",
                "Brasil", "https://example.com/logo.png", "Novo Técnico",
                "Nou Maracanã", LocalDate.of(2020, 1, 1)
        );
        TimeResponseDto response = new TimeResponseDto(
                1L, "Flamengo Updated", "FLA", TipoTime.CLUBE, "Rio de Janeiro",
                "Brasil", "https://example.com/logo.png", "Novo Técnico",
                "Nou Maracanã", LocalDate.of(2020, 1, 1), null, null
        );

        when(timeService.atualizarTime(any(), eq(1L))).thenReturn(response);

        mockMvc.perform(put("/times/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Flamengo Updated"))
                .andExpect(jsonPath("$.estadio").value("Nou Maracanã"));
    }

    @Test
    @WithMockUser
    void deletarTime_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/times/1"))
                .andExpect(status().isNoContent());

        verify(timeService).deletarTime(1L);
    }
}
