package br.com.statezone.dto;


import java.time.LocalDateTime;
import java.util.List;

public record CampeonatoResponseDto(
        Long id,

        String nome,

        String pais,

        String temporada,

        String logoUrl,

        LocalDateTime criadoEm,

        LocalDateTime atualizadoEm

) {
}
