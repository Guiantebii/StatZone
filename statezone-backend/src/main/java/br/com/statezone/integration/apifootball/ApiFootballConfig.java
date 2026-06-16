package br.com.statezone.integration.apifootball;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiFootballConfig {

    @Bean
    public WebClient apiFootballWebClient(
            @Value("${api-football.base-url}") String baseUrl,
            @Value("${api-football.api-key}") String apiKey
    ) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-apisports-key", apiKey)
                .build();
    }
}