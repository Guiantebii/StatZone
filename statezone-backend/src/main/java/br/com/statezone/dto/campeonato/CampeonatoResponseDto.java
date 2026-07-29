package br.com.statezone.dto.campeonato;


import java.time.LocalDateTime;
import java.util.List;

public record CampeonatoResponseDto(
        Long id,

        String nome,

        String pais,

        String temporada,

        String logoUrl,

        String tipoFormato,

        Integer amarelosParaSuspensao,

        String status,

        LocalDateTime criadoEm,

        LocalDateTime atualizadoEm,

        List<Long> timesIds
) {
}
