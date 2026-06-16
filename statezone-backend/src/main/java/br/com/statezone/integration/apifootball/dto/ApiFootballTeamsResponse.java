package br.com.statezone.integration.apifootball.dto;

import java.util.List;

public record ApiFootballTeamsResponse(
        List<TeamResponseDto> response
) {
}