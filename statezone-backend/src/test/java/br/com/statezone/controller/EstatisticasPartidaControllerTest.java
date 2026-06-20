package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.estatisticasJogador.EstatisticasPartidaResponseDto;
import br.com.statezone.mapper.EstatisticasPartidaMapper;
import br.com.statezone.model.EstatisticasPartida;
import br.com.statezone.repository.EstatisticasPartidaRepository;
import br.com.statezone.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static br.com.statezone.support.TestFixtures.estatisticasPartida;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.time;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstatisticasPartidaController.class)
@Import(TestSecurityConfig.class)
class EstatisticasPartidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstatisticasPartidaRepository repository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private EstatisticasPartidaMapper mapper;

    @Test
    @WithMockUser
    void get_deveRetornarEstatisticasDaPartida() throws Exception {
        var campeonato = campeonato(1L, 3);
        var mandante = time(10L, "Mandante");
        var visitante = time(11L, "Visitante");
        var partida = partida(30L, campeonato, mandante, visitante);
        EstatisticasPartida stats = estatisticasPartida(40L, partida);
        EstatisticasPartidaResponseDto response = new EstatisticasPartidaResponseDto(
                30L,
                55,
                45,
                10,
                8,
                4,
                3,
                2,
                1,
                5,
                6,
                1,
                0,
                3,
                7,
                1,
                2,
                4,
                5
        );

        when(repository.findByPartidaId(30L)).thenReturn(Optional.of(stats));
        when(mapper.toDto(stats)).thenReturn(response);

        mockMvc.perform(get("/estatisticas/{partidaId}", 30L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partidaId").value(30))
                .andExpect(jsonPath("$.posseBolaMandante").value(55))
                .andExpect(jsonPath("$.defesasVisitante").value(2))
                .andExpect(jsonPath("$.penaltisDefendidosVisitante").value(5));
    }
}
