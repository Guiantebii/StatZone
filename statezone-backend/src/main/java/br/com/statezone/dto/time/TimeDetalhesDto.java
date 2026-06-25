package br.com.statezone.dto.time;

import java.util.List;

public record TimeDetalhesDto(
        Long id,
        String nome,
        String sigla,
        String cidade,
        String pais,
        String escudoUrl,
        String tecnico,
        String estadio,

        Integer jogadores,
        Integer partidas,
        Integer vitorias,
        Integer empates,
        Integer derrotas,
        Integer golsMarcados,
        Integer golsSofridos,
        List<String> forma
) {}