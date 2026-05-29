package br.com.statezone.dto.estatisticasJogador;

public record EstatisticasJogadorResponseDto(
        Long jogadorId,
        String nomeJogador,
        String nomeTime,
        Integer gols,
        Integer assistencias,
        Integer finalizacoes,
        Integer cartoesAmarelos,
        Integer cartoesVermelhos,
        Integer faltasCometidas,
        Integer partidasJogadas
) {
}
