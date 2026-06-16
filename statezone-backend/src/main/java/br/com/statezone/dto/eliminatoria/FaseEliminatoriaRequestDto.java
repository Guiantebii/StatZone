package br.com.statezone.dto.eliminatoria;

import br.com.statezone.enums.FaseEnum;
import jakarta.validation.constraints.NotNull;

public record FaseEliminatoriaRequestDto(
        @NotNull(message = "Fase é obrigatória")
        FaseEnum fase,

        Boolean jogoUnico
) {}