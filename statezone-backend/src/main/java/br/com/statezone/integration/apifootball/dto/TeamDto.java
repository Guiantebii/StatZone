package br.com.statezone.integration.apifootball.dto;

public record TeamDto(
        Long id,
        String name,
        String code,
        String country,
        Integer founded,
        String logo
) {
}