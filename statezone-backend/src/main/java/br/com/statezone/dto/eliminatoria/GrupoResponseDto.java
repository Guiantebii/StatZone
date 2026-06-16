package br.com.statezone.dto.eliminatoria;

import br.com.statezone.dto.time.TimeResumoDto;

import java.util.List;

public record GrupoResponseDto(
        Long id,
        String nome,
        Long campeonatoId,
        String campeonatoNome,
        List<TimeResumoDto> times
) {}