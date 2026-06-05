package br.com.statezone.dto.time;

import java.util.List;

public record UltimasPartidasTimeResponseDto (
        Long timeId,
        List<String> forma
) {
}
