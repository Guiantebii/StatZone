package br.com.statezone.service;

import br.com.statezone.config.TestSecurityConfig;
import br.com.statezone.dto.suspensao.SuspensaoResponseDto;
import br.com.statezone.enums.MotivoSuspensao;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.mapper.SuspensaoMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Suspensao;
import br.com.statezone.repository.EstatisticasJogadorCampeonatoRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.SuspensaoRepository;
import br.com.statezone.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static br.com.statezone.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class SuspensaoServiceTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Mock
    private SuspensaoRepository suspensaoRepository;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private SuspensaoMapper suspensaoMapper;

    @Mock
    private EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;

    @InjectMocks
    private SuspensaoService service;

    @Test
    @WithMockUser
    void listarSuspensoesProximaRodada_deveMapearDtos() {
        Suspensao suspensao = new Suspensao();
        SuspensaoResponseDto dto = new SuspensaoResponseDto(
                20L,
                "Jogador",
                "foto.png",
                "Time",
                "escudo.png",
                12,
                MotivoSuspensao.ACUMULO_AMARELOS
        );

        when(partidaRepository.findProximaRodada(1L)).thenReturn(12);
        when(suspensaoRepository.findByCampeonatoIdAndRodadaSuspensao(1L, 12)).thenReturn(List.of(suspensao));
        when(suspensaoMapper.toDto(suspensao)).thenReturn(dto);

        List<SuspensaoResponseDto> result = service.listarSuspensoesProximaRodada(1L);

        assertThat(result).containsExactly(dto);
        verify(partidaRepository).findProximaRodada(1L);
        verify(suspensaoMapper).toDto(suspensao);
    }

    @Test
    @WithMockUser
    void registrarEventoDisciplinar_deveCriarSuspensaoPorAcumulacaoDeAmarelos() {
        Campeonato campeonato = campeonato(1L, 3);
        var mandante = time(10L, "Mandante");
        var visitante = time(11L, "Visitante");
        Jogador jogador = jogador(20L, mandante, "Jogador");
        Partida partidaAtual = partida(30L, campeonato, mandante, visitante);
        Partida proximaPartida = partida(31L, campeonato, mandante, visitante);
        proximaPartida.setRodada(12);

        EstatisticasJogadorCampeonato estatisticas = new EstatisticasJogadorCampeonato();
        estatisticas.setJogador(jogador);
        estatisticas.setCampeonato(campeonato);
        estatisticas.setAmarelosDesdeSuspensao(2);

        when(estatisticasJogadorCampeonatoRepository.findByJogadorIdAndCampeonatoId(20L, 1L))
                .thenReturn(Optional.of(estatisticas));
        when(partidaRepository.findProximasPartidasDoTime(eq(1L), eq(10L), any(Pageable.class)))
                .thenReturn(List.of(proximaPartida));
        when(suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoId(20L, 1L, 31L))
                .thenReturn(false);

        service.registrarEventoDisciplinar(jogador, partidaAtual, TipoEvento.CARTAO_AMARELO);

        assertThat(estatisticas.getAmarelosDesdeSuspensao()).isZero();
        verify(estatisticasJogadorCampeonatoRepository, org.mockito.Mockito.times(2)).save(estatisticas);

        ArgumentCaptor<Suspensao> captor = ArgumentCaptor.forClass(Suspensao.class);
        verify(suspensaoRepository).save(captor.capture());

        Suspensao salva = captor.getValue();
        assertThat(salva.getJogador()).isSameAs(jogador);
        assertThat(salva.getCampeonato()).isSameAs(campeonato);
        assertThat(salva.getMotivo()).isEqualTo(MotivoSuspensao.ACUMULO_AMARELOS);
        assertThat(salva.getPartidaAlvo()).isSameAs(proximaPartida);
        assertThat(salva.getRodadaSuspensao()).isEqualTo(12);
    }

    @Test
    @WithMockUser
    void registrarEventoDisciplinar_deveCriarSuspensaoPorVermelho() {
        Campeonato campeonato = campeonato(1L, 3);
        var mandante = time(10L, "Mandante");
        var visitante = time(11L, "Visitante");
        Jogador jogador = jogador(20L, mandante, "Jogador");
        Partida partidaAtual = partida(30L, campeonato, mandante, visitante);
        Partida proximaPartida = partida(31L, campeonato, mandante, visitante);
        proximaPartida.setRodada(12);

        when(partidaRepository.findProximasPartidasDoTime(eq(1L), eq(10L), any(Pageable.class)))
                .thenReturn(List.of(proximaPartida));
        when(suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoId(20L, 1L, 31L))
                .thenReturn(false);

        service.registrarEventoDisciplinar(jogador, partidaAtual, TipoEvento.CARTAO_VERMELHO);

        ArgumentCaptor<Suspensao> captor = ArgumentCaptor.forClass(Suspensao.class);
        verify(suspensaoRepository).save(captor.capture());
        Suspensao salva = captor.getValue();

        assertThat(salva.getMotivo()).isEqualTo(MotivoSuspensao.CARTAO_VERMELHO);
        assertThat(salva.getPartidaAlvo()).isSameAs(proximaPartida);
        assertThat(salva.getRodadaSuspensao()).isEqualTo(12);
        verifyNoInteractions(estatisticasJogadorCampeonatoRepository);
    }

    @Test
    @WithMockUser
    void suspenderSeNecessario_naoDeveDuplicarSuspensaoExistente() {
        Campeonato campeonato = campeonato(1L, 3);
        var mandante = time(10L, "Mandante");
        var visitante = time(11L, "Visitante");
        Jogador jogador = jogador(20L, mandante, "Jogador");
        Partida partidaAtual = partida(30L, campeonato, mandante, visitante);
        Partida proximaPartida = partida(31L, campeonato, mandante, visitante);
        proximaPartida.setRodada(12);

        when(partidaRepository.findProximasPartidasDoTime(eq(1L), eq(10L), any(Pageable.class)))
                .thenReturn(List.of(proximaPartida));
        when(suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoId(20L, 1L, 31L))
                .thenReturn(true);

        service.suspenderSeNecessario(jogador, partidaAtual, MotivoSuspensao.CARTAO_VERMELHO);

        verify(suspensaoRepository, never()).save(any(Suspensao.class));
    }

    @Test
    @WithMockUser
    void suspenderSeNecessario_quandoNaoExisteProximaPartida_deveSalvarSuspensaoAberta() {
        Campeonato campeonato = campeonato(1L, 3);
        var mandante = time(10L, "Mandante");
        var visitante = time(11L, "Visitante");
        Jogador jogador = jogador(20L, mandante, "Jogador");
        Partida partidaAtual = partida(30L, campeonato, mandante, visitante);

        when(partidaRepository.findProximasPartidasDoTime(eq(1L), eq(10L), any(Pageable.class)))
                .thenReturn(List.of());
        when(suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoIsNull(20L, 1L))
                .thenReturn(false);

        service.suspenderSeNecessario(jogador, partidaAtual, MotivoSuspensao.CARTAO_VERMELHO);

        ArgumentCaptor<Suspensao> captor = ArgumentCaptor.forClass(Suspensao.class);
        verify(suspensaoRepository).save(captor.capture());
        assertThat(captor.getValue().getPartidaAlvo()).isNull();
        assertThat(captor.getValue().getRodadaSuspensao()).isNull();
    }
}
