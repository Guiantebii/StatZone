package br.com.statezone.dto.escalacao;

import java.util.List;

public record EscalacaoPartidaListResponseDto(
        Long partidaId,
        List<EscalacaoPartidaResponseDto> titulares,
        List<EscalacaoPartidaResponseDto> reservas
) {}