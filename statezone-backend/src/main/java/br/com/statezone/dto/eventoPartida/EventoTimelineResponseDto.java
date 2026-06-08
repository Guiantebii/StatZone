package br.com.statezone.dto.eventoPartida;

import br.com.statezone.enums.TipoEvento;

public record EventoTimelineResponseDto(

        Long id,

        TipoEvento tipo,

        Integer minuto,

        Integer minutoExtra,

        String tempo,

        Long timeId,

        String nomeTime,

        Long jogadorId,

        String jogador,

        Long jogadorSecundarioId,

        String jogadorSecundario
) {
}