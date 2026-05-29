package br.com.statezone.dto.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeResponseDto(
        Long id,
        String nome,
        String sigla,
        String cidade,
        String pais,
        String escudoUrl,
        String tecnico,
        String estadio,
        LocalDate fundadoEm,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
