package br.com.statezone.service;

import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.integration.apifootball.ApiFootballClient;
import br.com.statezone.integration.apifootball.dto.ApiFootballPlayersResponse;
import br.com.statezone.integration.apifootball.dto.PlayerDto;
import br.com.statezone.integration.apifootball.dto.PlayersTeamDto;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Time;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.TimeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiFootballJogadorImportService {

    private final ApiFootballClient apiFootballClient;
    private final TimeRepository timeRepository;
    private final JogadorRepository jogadorRepository;

    @Transactional
    public void importarJogadores(Long timeId) {

        Time time = timeRepository.findById(timeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Time não encontrado"));

        importarJogadoresDoTime(time);
    }

    @Transactional
    @Scheduled(fixedDelay = 8000, initialDelay = 5000)
    public void importarJogadoresTodosTimes() {

        List<Time> times = timeRepository.findAll();

        for (Time time : times) {

            long quantidadeJogadores =
                    jogadorRepository.countByTimeId(time.getId());

            if (quantidadeJogadores > 0) {

                log.info("Pulando {} - {} jogadores já importados",
                        time.getNome(), quantidadeJogadores);

                continue;
            }

            try {

                importarJogadoresDoTime(time);

                log.info("Jogadores importados com sucesso para: {}", time.getNome());

            } catch (Exception e) {

                log.error("Erro ao importar jogadores do time: {}", time.getNome(), e);
            }
        }
    }

    private void importarJogadoresDoTime(Time time) {

        ApiFootballPlayersResponse response =
                apiFootballClient.buscarElencoTime(
                        time.getApiFootballId()
                );

        List<Jogador> jogadores = new ArrayList<>();

        for (PlayersTeamDto elenco : response.response()) {

            for (PlayerDto player : elenco.players()) {

                Jogador jogador = jogadorRepository
                        .findByApiFootballId(player.id())
                        .orElseGet(Jogador::new);

                jogador.setApiFootballId(player.id());
                jogador.setNome(player.name());
                jogador.setFotoUrl(player.photo());

                jogador.setTime(time);

                jogadores.add(jogador);
            }
        }

        jogadorRepository.saveAll(jogadores);
    }
}