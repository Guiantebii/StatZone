package br.com.statezone.dto.rankings;

public record SelecaoCampeonatoResponseDto(
        Long jogadorId,
        String posicao,
        String nomeJogador,
        String nomeTime,
        double score
) {
}
