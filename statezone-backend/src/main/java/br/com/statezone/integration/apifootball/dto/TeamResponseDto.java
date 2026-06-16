package br.com.statezone.integration.apifootball.dto;

public record TeamResponseDto(
        TeamDto team,
        VenueDto venue
) {
}