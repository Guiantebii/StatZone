package br.com.statezone.dto.eliminatoria;

import jakarta.validation.constraints.NotBlank;

public record GrupoRequestDto(
        @NotBlank(message = "Nome do grupo é obrigatório")
        String nome
) {}