package br.com.statezone.dto.rankings;

public record RankingCartaoAmareloResponseDto(
        Integer posicao,
        Long jogadorId,
        String nomeJogador,
        String nomeTime,
        String escudoTime,
        Integer cartoesAmarelos
) {
}
