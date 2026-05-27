package br.com.statezone.dto;

import br.com.statezone.enums.TipoEvento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EventoPartidaRequestDto(
        @NotNull(message = "Tipo do evento é obrigatório")
        TipoEvento tipoEvento,

        @NotNull(message = "Minuto é obrigatório")
        @Min(value = 1, message = "Minuto deve ser no mínimo 1")
        @Max(value = 120, message = "Minuto deve ser no máximo 120")
        Integer minuto,

        @Min(value = 1, message = "Minuto extra deve ser no mínimo 1")
        @Max(value = 30, message = "Minuto extra deve ser no máximo 30")
        Integer minutoExtra,

        String descricao,

        @NotNull(message = "Jogador é obrigatório")
        Long jogadorId
) {
}
