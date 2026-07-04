package br.com.statezone.service;

import br.com.statezone.integration.apifootball.dto.ApiFootballTeamsResponse;
import br.com.statezone.integration.apifootball.dto.TeamResponseDto;
import br.com.statezone.integration.apifootball.ApiFootballClient;
import br.com.statezone.model.Time;
import br.com.statezone.repository.TimeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiFootballImportService {

    private final ApiFootballClient apiFootballClient;
    private final TimeRepository timeRepository;

    @Transactional
    public void importarTimesBrasileirao() {

        ApiFootballTeamsResponse response =
                apiFootballClient.buscarTimesLiga(71L, 2024);

        List<Time> times = new ArrayList<>();

        for (TeamResponseDto item : response.response()) {

            Long apiId = item.team().id();

            Time time = timeRepository
                    .findByApiFootballId(apiId)
                    .orElseGet(Time::new);

            time.setApiFootballId(apiId);
            time.setNome(item.team().name());
            time.setSigla(item.team().code());
            time.setPais(item.team().country());
            time.setEscudoUrl(item.team().logo());

            if (item.venue() != null) {
                time.setCidade(item.venue().city());
                time.setEstadio(item.venue().name());
            }

            times.add(time);
        }

        timeRepository.saveAll(times);
    }
}