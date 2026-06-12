package br.com.statezone.dto.partida;

import jakarta.validation.constraints.NotNull;

public record PenaltisRequestDto(
        @NotNull(message = "Gols de pênaltis do mandante é obrigatório")
        Integer golsPenaltisMandante,

        @NotNull(message = "Gols de pênaltis do visitante é obrigatório")
        Integer golsPenaltisVisitante
) {}