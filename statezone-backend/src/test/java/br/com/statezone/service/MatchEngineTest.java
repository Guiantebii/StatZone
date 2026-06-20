package br.com.statezone.service;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.ranking.RankingCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;

import static br.com.statezone.support.TestFixtures.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class MatchEngineTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Mock
    private StatsEngine statsEngine;

    @Mock
    private CleanSheetEngine cleanSheetEngine;

    @Mock
    private SuspensionEngine suspensionEngine;

    @Mock
    private RankingCacheService rankingCacheService;

    @InjectMocks
    private MatchEngine matchEngine;

    @Test
    @WithMockUser
    void process_deveOrquestrarAsEtapasNaOrdem() {
        var campeonato = campeonato(1L, 3);
        var mandante = time(10L, "Mandante");
        var visitante = time(11L, "Visitante");
        var partida = partida(30L, campeonato, mandante, visitante);

        matchEngine.process(partida);

        org.mockito.InOrder order = inOrder(statsEngine, cleanSheetEngine, suspensionEngine, rankingCacheService);
        order.verify(statsEngine).process(partida);
        order.verify(cleanSheetEngine).process(partida);
        order.verify(suspensionEngine).process(partida);
        order.verify(rankingCacheService).recalcular(1L);
    }
}
