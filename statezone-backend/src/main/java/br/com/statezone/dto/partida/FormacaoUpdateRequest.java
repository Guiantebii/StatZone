package br.com.statezone.dto.partida;

import br.com.statezone.enums.Formacao;
import jakarta.validation.constraints.NotNull;

public record FormacaoUpdateRequest(
        @NotNull(message = "O time é obrigatório")
        Long timeId,

        @NotNull(message = "A formação é obrigatória")
        Formacao formacao
) {}
