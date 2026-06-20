package br.com.statezone.service;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.escalacao.EscalacaoPartidaListResponseDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaRequestDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaResponseDto;
import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.Posicao;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.EscalacaoPartida;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.mapper.EscalacaoPartidaMapper;
import br.com.statezone.repository.EscalacaoPartidaRepository;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.SuspensaoRepository;
import br.com.statezone.security.JwtService;
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

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.escalacaoPartida;
import static br.com.statezone.support.TestFixtures.jogador;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class EscalacaoPartidaServiceTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Mock
    private EscalacaoPartidaRepository escalacaoPartidaRepository;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private JogadorRepository jogadorRepository;

    @Mock
    private SuspensaoRepository suspensaoRepository;

    private EscalacaoPartidaService service;

    private final EscalacaoPartidaMapper escalacaoPartidaMapper = Mappers.getMapper(EscalacaoPartidaMapper.class);

    @BeforeEach
    void setUp() {
        service = new EscalacaoPartidaService(
                escalacaoPartidaRepository,
                partidaRepository,
                jogadorRepository,
                escalacaoPartidaMapper,
                suspensaoRepository
        );
    }

    @Test
    @WithMockUser
    void adicionarJogador_deveSalvarComDefaults() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador goleiro = jogador(20L, mandante, "Goleiro", Posicao.GOLEIRO, 1);
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.AGENDADA, 4, 0, 0);
        EscalacaoPartidaRequestDto request = new EscalacaoPartidaRequestDto(
                20L,
                FuncaoEscalacao.TITULAR,
                null,
                null
        );

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(jogadorRepository.findById(20L)).thenReturn(Optional.of(goleiro));
        when(suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoId(20L, 1L, 30L))
                .thenReturn(false);
        when(escalacaoPartidaRepository.existsByPartidaIdAndJogadorId(30L, 20L)).thenReturn(false);
        when(escalacaoPartidaRepository.save(any(EscalacaoPartida.class))).thenAnswer(invocation -> {
            EscalacaoPartida escalacao = invocation.getArgument(0);
            escalacao.setId(70L);
            return escalacao;
        });

        EscalacaoPartidaResponseDto response = service.adicionarJogador(30L, request);

        assertThat(response.id()).isEqualTo(70L);
        assertThat(response.funcao()).isEqualTo(FuncaoEscalacao.TITULAR);
        assertThat(response.posicao()).isEqualTo(Posicao.GOLEIRO);
        assertThat(response.numeroCamisa()).isEqualTo(1);
        assertThat(response.ativo()).isTrue();
    }

    @Test
    @WithMockUser
    void adicionarJogador_deveBloquearJogadorDeOutroTime() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Time outroTime = time(12L, "Outro");
        Jogador jogadorEstranho = jogador(20L, outroTime, "Estranho", Posicao.CENTROAVANTE, 9);
        Partida partida = partida(30L, campeonato, mandante, visitante);
        EscalacaoPartidaRequestDto request = new EscalacaoPartidaRequestDto(20L, FuncaoEscalacao.TITULAR, null, null);

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(jogadorRepository.findById(20L)).thenReturn(Optional.of(jogadorEstranho));

        assertThatThrownBy(() -> service.adicionarJogador(30L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Jogador não pertence aos times da partida");

        verify(escalacaoPartidaRepository, never()).save(any());
    }

    @Test
    @WithMockUser
    void adicionarJogador_deveBloquearQuandoSuspenso() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador jogadorSuspenso = jogador(20L, mandante, "Suspenso", Posicao.CENTROAVANTE, 9);
        Partida partida = partida(30L, campeonato, mandante, visitante);
        EscalacaoPartidaRequestDto request = new EscalacaoPartidaRequestDto(20L, FuncaoEscalacao.RESERVA, null, null);

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(jogadorRepository.findById(20L)).thenReturn(Optional.of(jogadorSuspenso));
        when(suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoId(20L, 1L, 30L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.adicionarJogador(30L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Jogador está suspenso para esta partida");
    }

    @Test
    @WithMockUser
    void adicionarJogador_deveBloquearDuplicidade() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador jogador = jogador(20L, mandante, "Jogador", Posicao.CENTROAVANTE, 9);
        Partida partida = partida(30L, campeonato, mandante, visitante);
        EscalacaoPartidaRequestDto request = new EscalacaoPartidaRequestDto(20L, FuncaoEscalacao.RESERVA, null, null);

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(jogadorRepository.findById(20L)).thenReturn(Optional.of(jogador));
        when(suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoId(20L, 1L, 30L))
                .thenReturn(false);
        when(escalacaoPartidaRepository.existsByPartidaIdAndJogadorId(30L, 20L)).thenReturn(true);

        assertThatThrownBy(() -> service.adicionarJogador(30L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Jogador já está na escalação desta partida");
    }

    @Test
    @WithMockUser
    void buscarEscalacao_deveSepararTitularesEReservas() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Partida partida = partida(30L, campeonato, mandante, visitante);
        Jogador titular = jogador(20L, mandante, "Titular", Posicao.CENTROAVANTE, 9);
        Jogador reserva = jogador(21L, mandante, "Reserva", Posicao.MEIO_CAMPO, 18);
        List<EscalacaoPartida> escalacoes = List.of(
                escalacaoPartida(1L, partida, titular, FuncaoEscalacao.TITULAR, Posicao.CENTROAVANTE, 9, true),
                escalacaoPartida(2L, partida, reserva, FuncaoEscalacao.RESERVA, Posicao.MEIO_CAMPO, 18, true)
        );

        when(partidaRepository.findById(30L)).thenReturn(Optional.of(partida));
        when(escalacaoPartidaRepository.findByPartidaIdWithJogador(30L)).thenReturn(escalacoes);

        EscalacaoPartidaListResponseDto response = service.buscarEscalacao(30L);

        assertThat(response.partidaId()).isEqualTo(30L);
        assertThat(response.titulares()).hasSize(1);
        assertThat(response.reservas()).hasSize(1);
        assertThat(response.titulares().get(0).jogadorId()).isEqualTo(20L);
        assertThat(response.reservas().get(0).jogadorId()).isEqualTo(21L);
    }
}
