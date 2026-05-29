package br.com.statezone.dto.rankings;

public record ArtilhariaResponseDto(
        Integer posicao,
        Long jogadorId,
        String nomeJogador,
        String nomeTime,
        String escudoTime,
        Integer gols
) {
}
