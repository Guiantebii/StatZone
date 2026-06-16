package br.com.statezone.integration.apifootball.dto;

import java.util.List;

public record ApiFootballPlayersResponse(
        List<PlayersTeamDto> response
) {}