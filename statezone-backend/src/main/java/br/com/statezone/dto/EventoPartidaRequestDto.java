package br.com.statezone.dto;

import br.com.statezone.enums.TipoEvento;

public record EventoPartidaRequestDto(
        TipoEvento tipoEvento,
        Integer minuto,
        Integer minutoExtra,
        String descricao,
        Long jogadorId
) {
}
