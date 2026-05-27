package br.com.statezone.dto;

import br.com.statezone.enums.PeForte;
import br.com.statezone.enums.Posicao;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JogadorRequestDto(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
        String nome,

        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        @NotBlank(message = "Nacionalidade é obrigatória")
        String nacionalidade,

        @NotNull(message = "Posição é obrigatória")
        Posicao posicao,

        @Min(value = 1, message = "Número da camisa deve ser no mínimo 1")
        @Max(value = 99, message = "Número da camisa deve ser no máximo 99")
        Integer numeroCamisa,

        @DecimalMin(value = "1.50", message = "Altura mínima é 1.50m")
        @DecimalMax(value = "2.50", message = "Altura máxima é 2.50m")
        BigDecimal altura,

        @DecimalMin(value = "40.0", message = "Peso mínimo é 40kg")
        @DecimalMax(value = "200.0", message = "Peso máximo é 200kg")
        BigDecimal peso,

        @DecimalMin(value = "0.0", message = "Valor de mercado não pode ser negativo")
        BigDecimal valorMercado,

        PeForte peForte,

        @Pattern(regexp = "^(https?://).*", message = "URL da foto deve ser válida")
        String fotoUrl,

        @NotNull(message = "Time é obrigatório")
        Long timeId

) {}