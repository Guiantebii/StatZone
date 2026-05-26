package br.com.statezone.dto;

public record ClassificacaoResponseDto(
        Long timeId,
        String nomeTime,
        Integer pontos,
        Integer partidas,
        Integer vitorias,
        Integer empates,
        Integer derrotas,
        Integer golsFeitos,
        Integer golsSofridos,
        Integer saldoGols,
        Integer posicao
) {
}
