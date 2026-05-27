package br.com.statezone.service;

import br.com.statezone.dto.PartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.TimeRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FixtureGeneratorService {

    private final CampeonatoRepository campeonatoRepository;
    private final TimeRepository timeRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMapper partidaMapper;

    public List<PartidaResponseDto> gerarPartida(Long campeonatoId) {

        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));

        boolean jaExiste = partidaRepository.existsByCampeonatoId(campeonatoId);
        if (jaExiste) {
            throw new ConflictException("Fixtures já foram geradas para esse campeonato");
        }

        List<Time> times = new ArrayList<>(campeonato.getTimes()); 

        if (times == null || times.size() < 2) {
            throw new BusinessException("Campeonato precisa ter pelo menos 2 times");
        }

        if (times.size() % 2 != 0) {
            times.add(null);
        }

        int n = times.size();
        int totalRodadas = n - 1;
        int jogosPorRodada = n / 2;

        List<Partida> partidasCriadas = new ArrayList<>();

        for (int rodada = 1; rodada <= totalRodadas; rodada++) {
            for (int j = 0; j < jogosPorRodada; j++) {

                Time mandante = times.get(j);
                Time visitante = times.get(n - 1 - j);

                if (mandante == null || visitante == null) continue;

                Partida partida = new Partida();
                partida.setCampeonato(campeonato);
                partida.setTimeMandante(mandante);
                partida.setTimeVisitante(visitante);
                partida.setRodada(rodada);
                partida.setGolsMandante(0);
                partida.setGolsVisitante(0);
                partida.setStatus(StatusPartida.AGENDADA);

                partidasCriadas.add(partidaRepository.save(partida));
            }


            Time ultimo = times.remove(n - 1);
            times.add(1, ultimo);
        }

        return partidasCriadas.stream()
                .map(partidaMapper::toDto)
                .toList();
    }
}
