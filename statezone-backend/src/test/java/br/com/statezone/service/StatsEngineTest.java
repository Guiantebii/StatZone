package br.com.statezone.service;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.EscalacaoPartida;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.EscalacaoPartidaRepository;
import br.com.statezone.repository.EstatisticasJogadorCampeonatoRepository;
import br.com.statezone.repository.EstatisticasJogadorRepository;
import br.com.statezone.repository.JogadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static br.com.statezone.support.TestFixtures.campeonato;
import static br.com.statezone.support.TestFixtures.escalacaoPartida;
import static br.com.statezone.support.TestFixtures.evento;
import static br.com.statezone.support.TestFixtures.jogador;
import static br.com.statezone.support.TestFixtures.partida;
import static br.com.statezone.support.TestFixtures.time;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsEngineTest {

    @Mock
    private EstatisticasJogadorRepository estatisticasJogadorRepository;

    @Mock
    private EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;

    @Mock
    private JogadorRepository jogadorRepository;

    @Mock
    private EscalacaoPartidaRepository escalacaoPartidaRepository;

    private StatsEngine engine;

    @BeforeEach
    void setUp() {
        engine = new StatsEngine(
                estatisticasJogadorRepository,
                estatisticasJogadorCampeonatoRepository,
                jogadorRepository,
                escalacaoPartidaRepository
        );
    }

    @Test
    void process_deveAtualizarEstatisticasComEscalacaoEIgnorarEventosAnulados() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador atacante = jogador(20L, mandante, "Atacante");
        Jogador assistente = jogador(21L, mandante, "Assistente");
        Jogador goleiro = jogador(22L, mandante, "Goleiro");
        Jogador defensor = jogador(23L, visitante, "Defensor");
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.ENCERRADA, 4, 2, 1);

        List<EscalacaoPartida> escalacoes = List.of(
                escalacaoPartida(1L, partida, atacante, null, atacante.getPosicao(), 9, true),
                escalacaoPartida(2L, partida, assistente, null, assistente.getPosicao(), 10, true),
                escalacaoPartida(3L, partida, goleiro, null, goleiro.getPosicao(), 1, true),
                escalacaoPartida(4L, partida, defensor, null, defensor.getPosicao(), 4, true)
        );

        EventoPartida gol = evento(100L, TipoEvento.GOL, 15, null, "Gol", atacante, assistente, mandante, false);
        EventoPartida finalizacao = evento(101L, TipoEvento.FINALIZACAO, 16, null, "Finalizacao", atacante, null, mandante, false);
        EventoPartida amarelo = evento(102L, TipoEvento.CARTAO_AMARELO, 17, null, "Amarelo", defensor, null, visitante, false);
        EventoPartida penaltiDefendido = evento(103L, TipoEvento.PENALTI_DEFENDIDO, 18, null, "Defesa", goleiro, null, mandante, false);
        EventoPartida golAnulado = evento(104L, TipoEvento.GOL, 19, null, "Anulado", atacante, assistente, mandante, true);
        List<EventoPartida> eventos = new java.util.ArrayList<>(List.of(gol, finalizacao, amarelo, penaltiDefendido, golAnulado));
        eventos.add(null);
        partida.setEventos(eventos);

        when(escalacaoPartidaRepository.findByPartidaIdWithJogador(30L)).thenReturn(escalacoes);

        Map<Long, EstatisticasJogador> carreiraStore = new HashMap<>();
        when(estatisticasJogadorRepository.findByJogadorId(anyLong())).thenAnswer(invocation -> {
            Long jogadorId = invocation.getArgument(0);
            return Optional.ofNullable(carreiraStore.get(jogadorId));
        });
        when(estatisticasJogadorRepository.save(org.mockito.ArgumentMatchers.any(EstatisticasJogador.class)))
                .thenAnswer(invocation -> {
                    EstatisticasJogador estatisticas = invocation.getArgument(0);
                    carreiraStore.put(estatisticas.getJogador().getId(), estatisticas);
                    return estatisticas;
                });

        Map<String, EstatisticasJogadorCampeonato> campeonatoStore = new HashMap<>();
        when(estatisticasJogadorCampeonatoRepository.findByJogadorIdAndCampeonatoId(anyLong(), eq(1L)))
                .thenAnswer(invocation -> {
                    Long jogadorId = invocation.getArgument(0);
                    return Optional.ofNullable(campeonatoStore.get(chave(jogadorId, 1L)));
                });
        when(estatisticasJogadorCampeonatoRepository.save(org.mockito.ArgumentMatchers.any(EstatisticasJogadorCampeonato.class)))
                .thenAnswer(invocation -> {
                    EstatisticasJogadorCampeonato estatisticas = invocation.getArgument(0);
                    campeonatoStore.put(chave(estatisticas.getJogador().getId(), estatisticas.getCampeonato().getId()), estatisticas);
                    return estatisticas;
                });

        engine.process(partida);

        assertThat(carreiraStore.get(20L).getPartidasJogadas()).isEqualTo(1);
        assertThat(carreiraStore.get(20L).getGols()).isEqualTo(1);
        assertThat(carreiraStore.get(20L).getFinalizacoes()).isEqualTo(1);
        assertThat(carreiraStore.get(21L).getAssistencias()).isEqualTo(1);
        assertThat(carreiraStore.get(22L).getDefesas()).isEqualTo(1);
        assertThat(carreiraStore.get(22L).getPenaltisDefendidos()).isEqualTo(1);
        assertThat(carreiraStore.get(23L).getCartoesAmarelos()).isEqualTo(1);
        assertThat(carreiraStore.get(23L).getPartidasJogadas()).isEqualTo(1);
        assertThat(campeonatoStore.get(chave(20L, 1L)).getGols()).isEqualTo(1);
        assertThat(campeonatoStore.get(chave(22L, 1L)).getPenaltisDefendidos()).isEqualTo(1);
    }

    @Test
    void process_quandoNaoHaEscalacao_deveUsarJogadoresDoTime() {
        Campeonato campeonato = campeonato(1L, 3);
        Time mandante = time(10L, "Mandante");
        Time visitante = time(11L, "Visitante");
        Jogador goleiroMandante = jogador(20L, mandante, "Goleiro", br.com.statezone.enums.Posicao.GOLEIRO, 1);
        Jogador atacanteVisitante = jogador(21L, visitante, "Atacante", br.com.statezone.enums.Posicao.CENTROAVANTE, 9);
        Partida partida = partida(30L, campeonato, mandante, visitante, StatusPartida.ENCERRADA, 4, 1, 0);
        partida.setEventos(List.of());

        when(escalacaoPartidaRepository.findByPartidaIdWithJogador(30L)).thenReturn(List.of());
        when(jogadorRepository.findByTimeIdIn(List.of(10L, 11L))).thenReturn(List.of(goleiroMandante, atacanteVisitante));

        Map<Long, EstatisticasJogador> carreiraStore = new HashMap<>();
        when(estatisticasJogadorRepository.findByJogadorId(anyLong())).thenAnswer(invocation -> {
            Long jogadorId = invocation.getArgument(0);
            return Optional.ofNullable(carreiraStore.get(jogadorId));
        });
        when(estatisticasJogadorRepository.save(org.mockito.ArgumentMatchers.any(EstatisticasJogador.class)))
                .thenAnswer(invocation -> {
                    EstatisticasJogador estatisticas = invocation.getArgument(0);
                    carreiraStore.put(estatisticas.getJogador().getId(), estatisticas);
                    return estatisticas;
                });

        Map<String, EstatisticasJogadorCampeonato> campeonatoStore = new HashMap<>();
        when(estatisticasJogadorCampeonatoRepository.findByJogadorIdAndCampeonatoId(anyLong(), eq(1L)))
                .thenAnswer(invocation -> {
                    Long jogadorId = invocation.getArgument(0);
                    return Optional.ofNullable(campeonatoStore.get(chave(jogadorId, 1L)));
                });
        when(estatisticasJogadorCampeonatoRepository.save(org.mockito.ArgumentMatchers.any(EstatisticasJogadorCampeonato.class)))
                .thenAnswer(invocation -> {
                    EstatisticasJogadorCampeonato estatisticas = invocation.getArgument(0);
                    campeonatoStore.put(chave(estatisticas.getJogador().getId(), estatisticas.getCampeonato().getId()), estatisticas);
                    return estatisticas;
                });

        engine.process(partida);

        assertThat(carreiraStore).containsKeys(20L, 21L);
        assertThat(carreiraStore.get(20L).getPartidasJogadas()).isEqualTo(1);
        assertThat(carreiraStore.get(21L).getPartidasJogadas()).isEqualTo(1);
        verify(jogadorRepository).findByTimeIdIn(List.of(10L, 11L));
    }

    private String chave(Long jogadorId, Long campeonatoId) {
        return jogadorId + ":" + campeonatoId;
    }
}
