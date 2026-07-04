package br.com.statezone.dto.partida;

import br.com.statezone.enums.StatusPartida;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PartidaRequestDto(

        @NotBlank(message = "O estádio é obrigatório")
        String estadio,

        @NotBlank(message = "O árbitro é obrigatório")
        String arbitro,

        @NotNull(message = "A rodada é obrigatória")
        @Min(value = 1, message = "A rodada deve ser maior que 0")
        Integer rodada,

        @NotNull(message = "A data da partida é obrigatória")
        LocalDateTime dataPartida,

        @NotNull(message = "O status é obrigatório")
        StatusPartida status,

        Integer golsMandante,

        Integer golsVisitante,

        @NotNull(message = "O campeonato é obrigatório")
        Long campeonatoId,

        @NotNull(message = "O time mandante é obrigatório")
        Long timeMandanteId,

        @NotNull(message = "O time visitante é obrigatório")
        Long timeVisitanteId
) {
}
