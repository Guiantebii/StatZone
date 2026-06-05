package br.com.statezone.dto.estatisticasJogador;

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
        Integer escanteiosVisitante,

        Integer cartoesAmarelosMandante,
        Integer cartoesAmarelosVisitante,

        Integer cartoesVermelhosMandante,
        Integer cartoesVermelhosVisitante,

        Integer defesasMandante,
        Integer defesasVisitante,

        Integer penaltisDefendidosMandante,
        Integer penaltisDefendidosVisitante
) {
}
