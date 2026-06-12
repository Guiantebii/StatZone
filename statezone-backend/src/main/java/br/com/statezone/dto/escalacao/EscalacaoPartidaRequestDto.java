package br.com.statezone.dto.escalacao;

import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.Posicao;
import jakarta.validation.constraints.NotNull;

public record EscalacaoPartidaRequestDto(
        @NotNull(message = "Jogador é obrigatório")
        Long jogadorId,

        @NotNull(message = "Função é obrigatória")
        FuncaoEscalacao funcao,

        Posicao posicao,

        Integer numeroCamisa
) {}