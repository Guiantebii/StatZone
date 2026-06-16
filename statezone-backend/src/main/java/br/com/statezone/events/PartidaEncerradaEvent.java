package br.com.statezone.events;

import br.com.statezone.model.Partida;
import lombok.Getter;

@Getter
public class PartidaEncerradaEvent {

    private final Partida partida;

    public PartidaEncerradaEvent(Partida partida) {
        this.partida = partida;
    }
}