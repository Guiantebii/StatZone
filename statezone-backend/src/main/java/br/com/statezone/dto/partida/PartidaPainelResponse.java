package br.com.statezone.dto.partida;

import java.util.List;

public record PartidaPainelResponse(
        List<PartidaResponseDto> aoVivo,
        List<PartidaResponseDto> agendadas,
        List<PartidaResponseDto> recentes,
        long total
) {
}
