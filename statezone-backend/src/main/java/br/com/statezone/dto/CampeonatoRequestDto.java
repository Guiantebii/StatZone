package br.com.statezone.dto;

public record CampeonatoRequestDto(
        String nome,
        String pais,
        String temporada,
        String logoUrl
) {
}
