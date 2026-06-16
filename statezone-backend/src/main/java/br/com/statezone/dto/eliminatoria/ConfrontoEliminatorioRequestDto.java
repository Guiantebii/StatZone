package br.com.statezone.dto.eliminatoria;

import jakarta.validation.constraints.NotNull;

public record ConfrontoEliminatorioRequestDto(

        @NotNull
        Long timeAId,

        @NotNull
        Long timeBId,


        Integer ordem,

        Integer chave,

        Integer slotTimeA,

        Integer slotTimeB,

        Long proximoConfrontoId,

        Integer slotProximo,

        Integer seed

) {}