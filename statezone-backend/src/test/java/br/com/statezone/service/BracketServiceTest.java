package br.com.statezone.service;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.eliminatoria.FaseEliminatoriaRequestDto;
import br.com.statezone.dto.eliminatoria.FaseEliminatoriaResponseDto;
import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.FaseEliminatoria;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Suspensao;
import br.com.statezone.model.Time;
import br.com.statezone.mapper.FaseEliminatoriaMapper;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.ConfrontoEliminatorioRepository;
import br.com.statezone.repository.FaseEliminatoriaRepository;
import br.com.statezone.repository.GrupoRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.SuspensaoRepository;
import br.com.statezone.security.JwtService;
import br.com.statezone.service.ranking.RankingEngine;
import br.com.statezone.service.helper.CampeonatoAccessHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.confrontoEliminatorio;
import static br.com.statezone.support.TestFixtures.faseEliminatoria;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.suspensao;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class BracketServiceTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Mock
    private FaseEliminatoriaRepository faseEliminatoriaRepository;

    @Mock
    private ConfrontoEliminatorioRepository confrontoEliminatorioRepository;

    @Mock
    private CampeonatoRepository campeonatoRepository;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private BracketEngine bracketEngine;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private SuspensaoRepository suspensaoRepository;

    @Mock
    private RankingEngine rankingEngine;

    @Mock
    private CampeonatoAccessHelper campeonatoAccessHelper;

    private BracketService service;

    private final FaseEliminatoriaMapper faseEliminatoriaMapper = Mappers.getMapper(FaseEliminatoriaMapper.class);

    @BeforeEach
    void setUp() {
        service = new BracketService(
                faseEliminatoriaRepository,
                confrontoEliminatorioRepository,
                campeonatoRepository,
                partidaRepository,
                bracketEngine,
                faseEliminatoriaMapper,
                grupoRepository,
                suspensaoRepository,
                rankingEngine,
                campeonatoAccessHelper
        );
    }

    @Test
    @WithMockUser
    void criarFase_deveSalvarFase() {
        Campeonato campeonato = campeonato(1L, 3);
        FaseEliminatoria fase = faseEliminatoria(10L, campeonato, FaseEnum.OITAVAS);

        when(campeonatoRepository.findById(1L)).thenReturn(Optional.of(campeonato));
        when(faseEliminatoriaRepository.save(any(FaseEliminatoria.class))).thenReturn(fase);

        FaseEliminatoriaResponseDto response = service.criarFase(1L, new FaseEliminatoriaRequestDto(FaseEnum.OITAVAS, true));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.campeonatoId()).isEqualTo(1L);
        assertThat(response.fase()).isEqualTo(FaseEnum.OITAVAS);
        assertThat(response.jogoUnico()).isTrue();
    }

    @Test
    @WithMockUser
    void gerarPrimeiraFase_deveCriarPartidasEResolverSuspensoesPendentes() {
        Campeonato campeonato = campeonato(1L, 3);
        Time a = time(10L, "A");
        Time b = time(11L, "B");
        Time c = time(12L, "C");
        Time d = time(13L, "D");
        campeonato.setTimes(List.of(a, b, c, d));

        FaseEliminatoria fase = faseEliminatoria(10L, campeonato, FaseEnum.OITAVAS);
        ConfrontoEliminatorio confronto1 = confrontoEliminatorio(100L, fase, a, d);
        ConfrontoEliminatorio confronto2 = confrontoEliminatorio(101L, fase, b, c);

        Suspensao pendente = suspensao(1L, null, campeonato, br.com.statezone.enums.MotivoSuspensao.CARTAO_VERMELHO, null, null);
        pendente.setJogador(br.com.statezone.support.TestFixtures.jogador(20L, a, "Suspenso"));

        when(faseEliminatoriaRepository.findById(10L)).thenReturn(Optional.of(fase));
        when(confrontoEliminatorioRepository.findByFaseEliminatoriaIdOrderByBracketIndexAsc(10L)).thenReturn(List.of());
        when(grupoRepository.findByCampeonatoIdWithTimes(1L)).thenReturn(List.of());
        when(bracketEngine.gerarFaseInicial(anyList(), eq(fase))).thenReturn(List.of(confronto1, confronto2));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(new org.mockito.stubbing.Answer<Partida>() {
            private final AtomicLong sequence = new AtomicLong(200L);

            @Override
            public Partida answer(org.mockito.invocation.InvocationOnMock invocation) {
                Partida saved = invocation.getArgument(0);
                saved.setId(sequence.getAndIncrement());
                return saved;
            }
        });
        when(suspensaoRepository.findByCampeonatoIdAndJogador_Time_IdAndPartidaAlvoIsNull(1L, 10L))
                .thenReturn(List.of(pendente));
        when(suspensaoRepository.findByCampeonatoIdAndJogador_Time_IdAndPartidaAlvoIsNull(1L, 11L))
                .thenReturn(List.of());
        when(suspensaoRepository.findByCampeonatoIdAndJogador_Time_IdAndPartidaAlvoIsNull(1L, 12L))
                .thenReturn(List.of());
        when(suspensaoRepository.findByCampeonatoIdAndJogador_Time_IdAndPartidaAlvoIsNull(1L, 13L))
                .thenReturn(List.of());

        service.gerarPrimeiraFase(1L, 10L, 2);

        ArgumentCaptor<Partida> partidaCaptor = ArgumentCaptor.forClass(Partida.class);
        verify(partidaRepository, org.mockito.Mockito.times(2)).save(partidaCaptor.capture());
        assertThat(partidaCaptor.getAllValues())
                .allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(StatusPartida.AGENDADA));
        assertThat(pendente.getPartidaAlvo()).isSameAs(partidaCaptor.getAllValues().get(0));

        ArgumentCaptor<Suspensao> suspensaoCaptor = ArgumentCaptor.forClass(Suspensao.class);
        verify(suspensaoRepository).save(suspensaoCaptor.capture());
        assertThat(suspensaoCaptor.getValue().getPartidaAlvo()).isSameAs(partidaCaptor.getAllValues().get(0));
        verify(confrontoEliminatorioRepository).saveAll(List.of(confronto1, confronto2));
    }

    @Test
    @WithMockUser
    void encerraConfronto_deveClassificarVencedorEEncerrarConfronto() {
        Campeonato campeonato = campeonato(1L, 3);
        Time a = time(10L, "A");
        Time b = time(11L, "B");
        FaseEliminatoria fase = faseEliminatoria(10L, campeonato, FaseEnum.OITAVAS);
        ConfrontoEliminatorio confronto = confrontoEliminatorio(100L, fase, a, b);
        Partida ida = partida(200L, campeonato, a, b, StatusPartida.ENCERRADA, 1, 2, 0);
        confronto.setPartidaIda(ida);
        confronto.setStatusConfronto(StatusConfronto.PENDENTE);

        when(confrontoEliminatorioRepository.findById(100L)).thenReturn(Optional.of(confronto));
        when(bracketEngine.resolverVencedor(confronto)).thenReturn(a);
        when(confrontoEliminatorioRepository.findByFaseEliminatoriaIdOrderByBracketIndexAsc(10L)).thenReturn(List.of(confronto));
        when(confrontoEliminatorioRepository.save(any(ConfrontoEliminatorio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FaseEliminatoriaResponseDto response = service.encerraConfronto(1L, 100L);

        assertThat(confronto.getStatusConfronto()).isEqualTo(StatusConfronto.ENCERRADO);
        assertThat(response.id()).isEqualTo(10L);
        verify(bracketEngine).resolverVencedor(confronto);
        verify(faseEliminatoriaRepository, never()).save(any());
    }
}
