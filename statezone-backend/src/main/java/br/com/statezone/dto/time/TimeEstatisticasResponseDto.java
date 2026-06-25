package br.com.statezone.dto.time;

public record TimeEstatisticasResponseDto(
        Long timeId,
        Integer partidas,
        Integer vitorias,
        Integer empates,
        Integer derrotas,
        Integer golsMarcados,
        Integer golsSofridos
) {}
