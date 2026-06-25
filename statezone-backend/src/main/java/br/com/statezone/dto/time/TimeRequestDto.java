package br.com.statezone.dto.time;

import br.com.statezone.model.TipoTime;
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

        TipoTime tipo,

        String cidade,

        @NotBlank(message = "País é obrigatório")
        String pais,

        String escudoUrl,

        String tecnico,

        String estadio,

        @Past(message = "Data de fundação deve estar no passado")
        LocalDate fundadoEm
) {
}