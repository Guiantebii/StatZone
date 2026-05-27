package br.com.statezone.events;

import br.com.statezone.model.Partida;

public record RankingAtualizadoEvent(
        Partida partida
) {
}
