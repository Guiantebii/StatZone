package br.com.statezone.dto.partida;

import br.com.statezone.enums.StatusPartida;

import java.time.LocalDateTime;

public record PartidaResponseDto(
        Long id,
        String estadio,
        String arbitro,
        Integer rodada,
        LocalDateTime dataPartida,
        StatusPartida status,

        Integer golsMandante,
        Integer golsVisitante,

        Long campeonatoId,
        String campeonatoNome,

        Long timeMandanteId,
        String timeMandanteNome,

        Long timeVisitanteId,
        String timeVisitanteNome,

        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
        ) {
}
