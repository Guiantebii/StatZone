package br.com.statezone.service;

import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.service.helper.RoundRobinHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.PartidaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FixtureGeneratorService {

    private final CampeonatoRepository campeonatoRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMapper partidaMapper;
    private final RoundRobinHelper roundRobinHelper;

    @Transactional
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

        int totalRodadas = times.size() - 1;

        List<Partida> partidasCriadas = new ArrayList<>();

        roundRobinHelper.gerarTurno(times, 1, (mandante, visitante) -> {
            Partida partida = new Partida();
            partida.setCampeonato(campeonato);
            partida.setTimeMandante(mandante);
            partida.setTimeVisitante(visitante);
            partida.setRodada(partidasCriadas.size() % (totalRodadas) + 1);
            partida.setGolsMandante(0);
            partida.setGolsVisitante(0);
            partida.setStatus(StatusPartida.AGENDADA);
            partidasCriadas.add(partidaRepository.save(partida));
        });

        List<Partida> partidasReturno = new ArrayList<>();

        for (Partida partidaIda : partidasCriadas) {
            Partida partidaVolta = new Partida();
            partidaVolta.setCampeonato(campeonato);
            partidaVolta.setTimeMandante(partidaIda.getTimeVisitante());
            partidaVolta.setTimeVisitante(partidaIda.getTimeMandante());
            partidaVolta.setRodada(partidaIda.getRodada() + totalRodadas);
            partidaVolta.setGolsMandante(0);
            partidaVolta.setGolsVisitante(0);
            partidaVolta.setStatus(StatusPartida.AGENDADA);
            partidasReturno.add(partidaRepository.save(partidaVolta));
        }

        partidasCriadas.addAll(partidasReturno);

        return partidasCriadas.stream()
                .map(partidaMapper::toDto)
                .toList();
    }
}
