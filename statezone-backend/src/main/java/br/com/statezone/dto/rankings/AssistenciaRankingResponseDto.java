package br.com.statezone.dto.rankings;

public record AssistenciaRankingResponseDto (
        Integer posicao,
        Long jogadorId,
        String nomeJogador,
        String nomeTime,
        String escudoTime,
        Integer assistencias
){
}
