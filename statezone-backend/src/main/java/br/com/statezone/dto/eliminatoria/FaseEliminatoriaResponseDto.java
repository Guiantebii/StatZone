package br.com.statezone.dto.eliminatoria;

import br.com.statezone.enums.FaseEnum;

import java.util.List;

public record FaseEliminatoriaResponseDto(
        Long id,
        Long campeonatoId,
        FaseEnum fase,
        Boolean jogoUnico,
        List<ConfrontoEliminatorioResponseDto> confrontos
) {}
