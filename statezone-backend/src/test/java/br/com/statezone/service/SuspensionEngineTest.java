package br.com.statezone.service;

import br.com.statezone.enums.TipoEvento;
import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.Partida;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static br.com.statezone.support.TestFixtures.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SuspensionEngineTest {

    @Mock
    private SuspensaoService suspensaoService;

    @InjectMocks
    private SuspensionEngine suspensionEngine;

    @Test
    void process_deveIgnorarEventosAnuladosENulos() {
        var campeonato = campeonato(1L, 3);
        var mandante = time(10L, "Mandante");
        var visitante = time(11L, "Visitante");
        var jogador = jogador(20L, mandante, "Jogador");
        Partida partida = partida(30L, campeonato, mandante, visitante);

        EventoPartida amarelo = evento(100L, TipoEvento.CARTAO_AMARELO, jogador, mandante, false);
        EventoPartida vermelhoAnulado = evento(101L, TipoEvento.CARTAO_VERMELHO, jogador, mandante, true);
        EventoPartida semTipo = new EventoPartida();

        List<EventoPartida> eventos = new ArrayList<>();
        eventos.add(null);
        eventos.add(vermelhoAnulado);
        eventos.add(semTipo);
        eventos.add(amarelo);
        partida.setEventos(eventos);

        suspensionEngine.process(partida);

        verify(suspensaoService).registrarEventoDisciplinar(jogador, partida, TipoEvento.CARTAO_AMARELO);
        verify(suspensaoService, never()).registrarEventoDisciplinar(eq(jogador), eq(partida), eq(TipoEvento.CARTAO_VERMELHO));
        verifyNoMoreInteractions(suspensaoService);
    }
}
