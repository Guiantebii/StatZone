package br.com.statezone.dto.eliminatoria;

import br.com.statezone.dto.time.TimeResumoDto;
import br.com.statezone.enums.StatusConfronto;

public record ConfrontoEliminatorioResponseDto(
        Long id,
        TimeResumoDto timeA,
        TimeResumoDto timeB,
        Long partidaIdaId,
        Long partidaVoltaId,
        TimeResumoDto timeClassificado,
        StatusConfronto statusConfronto
) {}