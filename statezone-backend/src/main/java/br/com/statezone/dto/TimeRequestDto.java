package br.com.statezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TimeRequestDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100)
        String nome,

        @NotBlank(message = "Sigla é obrigatória")
        @Size(min = 2, max = 5)
        String sigla,

        @NotBlank(message = "Cidade é obrigatória")
        String cidade,

        @NotBlank(message = "País é obrigatório")
        String pais,

        String escudoUrl,

        @NotBlank(message = "Técnico é obrigatório")
        String tecnico,

        @NotBlank(message = "Estádio é obrigatório")
        String estadio,

        @Past(message = "Data de fundação deve estar no passado")
        LocalDate fundadoEm
) {
}