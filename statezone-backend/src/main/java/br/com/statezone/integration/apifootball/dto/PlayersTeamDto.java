package br.com.statezone.integration.apifootball.dto;

import java.util.List;

public record PlayersTeamDto(
        TeamDto team,
        List<PlayerDto> players
) {}