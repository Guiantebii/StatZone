package br.com.statezone.dto.time;

import br.com.statezone.model.TipoTime;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeResponseDto(
        Long id,
        String nome,
        String sigla,
        TipoTime tipo,
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
