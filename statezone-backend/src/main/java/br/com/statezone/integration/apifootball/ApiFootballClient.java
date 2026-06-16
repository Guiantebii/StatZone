package br.com.statezone.integration.apifootball;

import br.com.statezone.integration.apifootball.dto.ApiFootballPlayersResponse;
import br.com.statezone.integration.apifootball.dto.ApiFootballTeamsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class ApiFootballClient {

    private final WebClient apiFootballWebClient;

    public ApiFootballTeamsResponse buscarTimesLiga(
            Long leagueId,
            Integer season
    ) {
        return apiFootballWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("league", leagueId)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .bodyToMono(ApiFootballTeamsResponse.class)
                .block();
    }

    public ApiFootballPlayersResponse buscarElencoTime(
            Long teamId
    ) {
        return apiFootballWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/players/squads")
                        .queryParam("team", teamId)
                        .build())
                .retrieve()
                .bodyToMono(ApiFootballPlayersResponse.class)
                .block();
    }
}