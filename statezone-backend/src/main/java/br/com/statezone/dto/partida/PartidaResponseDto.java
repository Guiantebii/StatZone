package br.com.statezone.dto.partida;

import br.com.statezone.enums.Formacao;
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

        Integer golsPenaltisMandante,
        Integer golsPenaltisVisitante,

        Long campeonatoId,
        String campeonatoNome,

        Long timeMandanteId,
        String timeMandanteNome,

        Long timeVisitanteId,
        String timeVisitanteNome,

        String escudoMandante,
        String escudoVisitante,

        Formacao formacaoMandante,
        Formacao formacaoVisitante,

        Long grupoId,
        Long faseEliminatoriaId,

        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
        ) {

    // Backwards-compatible constructor used by legacy tests/code which don't provide
    // penaltis/formacoes/grupo/fase fields. Delegates to canonical constructor.
    public PartidaResponseDto(Long id,
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
                              String escudoMandante,
                              String escudoVisitante,
                              LocalDateTime criadoEm,
                              LocalDateTime atualizadoEm) {
        this(id,
                estadio,
                arbitro,
                rodada,
                dataPartida,
                status,
                golsMandante,
                golsVisitante,
                null, // golsPenaltisMandante
                null, // golsPenaltisVisitante
                campeonatoId,
                campeonatoNome,
                timeMandanteId,
                timeMandanteNome,
                timeVisitanteId,
                timeVisitanteNome,
                escudoMandante,
                escudoVisitante,
                null, // formacaoMandante
                null, // formacaoVisitante
                null, // grupoId
                null, // faseEliminatoriaId
                criadoEm,
                atualizadoEm);
    }
}
