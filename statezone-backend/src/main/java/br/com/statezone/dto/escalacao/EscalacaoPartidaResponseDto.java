package br.com.statezone.dto.escalacao;

import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.Posicao;

public record EscalacaoPartidaResponseDto(
        Long id,
        Long jogadorId,
        String nomeJogador,
        String fotoUrl,
        String nomeTime,
        String escudoTime,
        FuncaoEscalacao funcao,
        Posicao posicao,
        Integer numeroCamisa,
        Boolean ativo
) {}