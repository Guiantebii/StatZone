package br.com.statezone.dto;

import br.com.statezone.enums.TipoEvento;

public record EventoPartidaResponseDto(
        Long id,

        TipoEvento tipoEvento,

        Integer minuto,

        Integer minutoExtra,

        String descricao,

        Long partidaId,

        Long jogadorId,

        String nomeJogador,

        String nomeTime
) {
}
