package br.com.statezone.events;

import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.Partida;

public record EventoPartidaCriadaEvent(
        EventoPartida evento,
        Partida partida
) {
}
