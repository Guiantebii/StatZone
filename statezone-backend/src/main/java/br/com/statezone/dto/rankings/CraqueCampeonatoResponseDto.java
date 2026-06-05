package br.com.statezone.dto.rankings;

public record CraqueCampeonatoResponseDto(
        Long jogadorId,
        String nomeJogador,
        String nomeTime,
        String escudoTime,
        Integer gols,
        Integer assistencias,
        Integer defesas,
        Integer penaltisDefendidos,
        Integer penaltisPerdidos,
        Integer cartoesAmarelos,
        Integer cartoesVermelhos,
        Double score
) {
}
