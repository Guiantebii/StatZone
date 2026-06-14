package br.com.statezone.dto.rankings;

public record RankingGoleiroResponseDto(
        Integer posicao,
        Long jogadorId,
        String nomeJogador,
        String fotoUrl,
        String nomeTime,
        String escudoTime,
        Integer cleanSheets,
        Integer defesas,
        Integer penaltisDefendidos,
        Integer partidasJogadas
) {}