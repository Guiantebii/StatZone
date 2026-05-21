package br.com.statezone.dto;

import br.com.statezone.enums.PeForte;
import br.com.statezone.enums.Posicao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JogadorRequestDto(
        String nome,
        LocalDate dataNascimento,
        String nacionalidade,
        Posicao posicao,
        Integer numeroCamisa,
        BigDecimal altura,
        BigDecimal peso,
        BigDecimal valorMercado,
        PeForte peForte,
        String fotoUrl,
        Long timeId
) {
}
