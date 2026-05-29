package br.com.statezone.dto.rankings;

public record SelecaoCampeonatoResponseDto(
        String posicao,
        String nomeJogador,
        String nomeTime,
        double score
) {
}
