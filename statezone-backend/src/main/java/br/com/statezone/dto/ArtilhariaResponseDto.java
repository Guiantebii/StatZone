package br.com.statezone.dto;

public record ArtilhariaResponseDto(
        Integer posicao,
        Long jogadorId,
        String nomeJogador,
        String nomeTime,
        String escudoTime,
        Integer gols
) {
}
