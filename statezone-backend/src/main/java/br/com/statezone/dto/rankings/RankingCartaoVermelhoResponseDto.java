package br.com.statezone.dto.rankings;

public record RankingCartaoVermelhoResponseDto(
        Integer posicao,
        Long jogadorId,
        String nomeJogador,
        String nomeTime,
        String escudoTime,
        Integer cartoesVermelhos
) {
}
