package br.com.statezone.controller;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.ApiFootballImportService;
import br.com.statezone.service.ApiFootballJogadorImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportacaoController.class)
@Import(TestSecurityConfig.class)
class ImportacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private ApiFootballImportService apiFootballImportService;

    @MockBean
    private ApiFootballJogadorImportService jogadorImportService;

    @Test
    @WithMockUser
    void importarTimes_deveRetornar200() throws Exception {
        mockMvc.perform(post("/api/importacao/times"))
                .andExpect(status().isOk());

        verify(apiFootballImportService).importarTimesBrasileirao();
    }

    @Test
    @WithMockUser
    void importarJogadores_deveRetornar200() throws Exception {
        mockMvc.perform(post("/api/importacao/jogadores/{timeId}", 1L))
                .andExpect(status().isOk());

        verify(jogadorImportService).importarJogadores(1L);
    }

    @Test
    @WithMockUser
    void importarJogadoresTodos_deveRetornar200() throws Exception {
        mockMvc.perform(post("/api/importacao/importar-jogadores-todos"))
                .andExpect(status().isOk());

        verify(jogadorImportService).importarJogadoresTodosTimes();
    }
}
