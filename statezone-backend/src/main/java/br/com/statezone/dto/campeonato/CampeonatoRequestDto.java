package br.com.statezone.dto.campeonato;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CampeonatoRequestDto(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O país é obrigatório")
        @Size(max = 50, message = "O país deve ter no máximo 50 caracteres")
        String pais,

        @NotBlank(message = "A temporada é obrigatória")
        @Size(max = 20, message = "A temporada deve ter no máximo 20 caracteres")
        String temporada,

        @NotNull(message = "O tipo de formato é obrigatório")
        String tipoFormato,

        @NotBlank(message = "A logo URL é obrigatória")
        String logoUrl,

        @Min(value = 1, message = "A quantidade de amarelos para suspensão deve ser no mínimo 1")
        Integer amarelosParaSuspensao
) {
}
