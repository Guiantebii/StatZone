package br.com.statezone.dto;

public record EstatisticasPartidaResponseDto(
        Long partidaId,

        Integer posseBolaMandante,
        Integer posseBolaVisitante,

        Integer finalizacoesMandante,
        Integer finalizacoesVisitante,

        Integer finalizacoesGolMandante,
        Integer finalizacoesGolVisitante,

        Integer faltasMandante,
        Integer faltasVisitante,

        Integer escanteiosMandante,
        Integer escanteiosVisitante
) {
}
