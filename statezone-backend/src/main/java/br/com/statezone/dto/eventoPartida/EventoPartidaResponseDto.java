package br.com.statezone.dto.eventoPartida;

import br.com.statezone.enums.TipoEvento;

public record EventoPartidaResponseDto(
        Long id,
        TipoEvento tipoEvento,
        Integer minuto,
        Integer minutoExtra,
        String descricao,

        Long eventoRelacionadoId,
        Boolean anulado,

        Long partidaId,
        Long jogadorId,
        String nomeJogador,
        String nomeTime,
        Long assistenteId,
        String nomeAssistente
) {
}