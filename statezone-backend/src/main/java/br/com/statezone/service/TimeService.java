package br.com.statezone.service;

import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.dto.time.TimeEstatisticasResponseDto;
import br.com.statezone.dto.time.TimePartidasResponseDto;
import br.com.statezone.dto.time.TimeRequestDto;
import br.com.statezone.dto.time.TimeResponseDto;
import br.com.statezone.dto.time.UltimasPartidasTimeResponseDto;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.JogadorMapper;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.mapper.TimeMapper;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.TimeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeService {
    private final TimeRepository timeRepository;
    private final TimeMapper timeMapper;
    private final JogadorRepository jogadorRepository;
    private final JogadorMapper jogadorMapper;
    private final PartidaRepository partidaRepository;
    private final PartidaMapper partidaMapper;

    public TimeResponseDto criar(TimeRequestDto dto){
        Time entity = timeMapper.toEntity(dto);

        Time salvo = timeRepository.save(entity);
        return timeMapper.toDto(salvo);
    }

    public List<TimeResponseDto> listarTodosTimes(){
        return timeRepository.findAll()
                .stream()
                .map(timeMapper::toDto)
                .toList();
    }

    public List<TimeResponseDto> buscarPorNome(String nome) {
        return timeRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(timeMapper::toDto)
                .toList();
    }

    public TimeResponseDto obterTimePorId(Long id) {

        Time time = timeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Time com id " + id + " não encontrado"
                        )
                );

        return timeMapper.toDto(time);
    }

    public TimeResponseDto atualizarTime(TimeRequestDto dto, Long id){
        Time time = timeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Time com id " + id + " não encontrado"
                        )
                );
        timeMapper.updateTimeFromDto(dto, time);

        Time timeAtualizado = timeRepository.save(time);

        return timeMapper.toDto(timeAtualizado);
    }

    public void deletarTime(Long id) {
        synchronized (this) {
            Time time = timeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Time com id " + id + " não encontrado"));

            List<String> dependencias = new ArrayList<>();

            if (time.getJogadores() != null && !time.getJogadores().isEmpty()) {
                dependencias.add(time.getJogadores().size() + " jogador(es)");
            }
            if (time.getPartidasMandante() != null && !time.getPartidasMandante().isEmpty()) {
                dependencias.add(time.getPartidasMandante().size() + " partida(s) como mandante");
            }
            if (time.getPartidasVisitante() != null && !time.getPartidasVisitante().isEmpty()) {
                dependencias.add(time.getPartidasVisitante().size() + " partida(s) como visitante");
            }
            if (timeRepository.countCampeonatosByTimeId(id) > 0) {
                dependencias.add("vinculado a campeonato(s)");
            }

            if (!dependencias.isEmpty()) {
                throw new BusinessException(
                        "Não é possível excluir o time '" + time.getNome() +
                                "'. Remova primeiro: " + String.join(", ", dependencias) + "."
                );
            }

            timeRepository.delete(time);
        }
    }
    public List<JogadorResponseDto> listarJogadoresPorTime(Long timeId) {

        timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado"));

        return jogadorRepository.findByTimeId(timeId)
                .stream()
                .map(jogadorMapper::toDto)
                .toList();
    }


    public UltimasPartidasTimeResponseDto ultimas5Partidas (Long timeId){
        timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado"));

        List<String> forma = partidaRepository
                .findUltimasPartidas(timeId, PageRequest.of(0, 5))
                .stream()
                .map(partida -> calcularResultado(partida, timeId))
                .toList();
    return new UltimasPartidasTimeResponseDto(timeId,forma);
    }

    public TimePartidasResponseDto obterPartidas(Long timeId) {
        timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado"));

        List<PartidaResponseDto> ultimas = partidaRepository
                .findUltimasPartidasComTimes(timeId, PageRequest.of(0, 5))
                .stream()
                .map(partidaMapper::toDto)
                .toList();

        List<PartidaResponseDto> proximas = partidaRepository
                .findProximasPartidas(timeId, PageRequest.of(0, 5))
                .stream()
                .map(partidaMapper::toDto)
                .toList();

        return new TimePartidasResponseDto(timeId, ultimas, proximas);
    }

    public TimeEstatisticasResponseDto obterEstatisticas(Long timeId) {
        timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado"));

        List<Partida> partidas = partidaRepository
                .findUltimasPartidas(timeId, Pageable.unpaged());

        int total = partidas.size();
        int vitorias = 0;
        int empates = 0;
        int derrotas = 0;
        int golsMarcados = 0;
        int golsSofridos = 0;

        for (Partida p : partidas) {
            boolean mandante = p.getTimeMandante().getId().equals(timeId);
            int golsTime = mandante ? (p.getGolsMandante() != null ? p.getGolsMandante() : 0) : (p.getGolsVisitante() != null ? p.getGolsVisitante() : 0);
            int golsAdv = mandante ? (p.getGolsVisitante() != null ? p.getGolsVisitante() : 0) : (p.getGolsMandante() != null ? p.getGolsMandante() : 0);

            golsMarcados += golsTime;
            golsSofridos += golsAdv;

            if (golsTime > golsAdv) vitorias++;
            else if (golsTime < golsAdv) derrotas++;
            else empates++;
        }

        return new TimeEstatisticasResponseDto(
                timeId, total, vitorias, empates, derrotas,
                golsMarcados, golsSofridos
        );
    }

    private String calcularResultado(Partida partida, Long timeId) {
        boolean mandante = partida.getTimeMandante().getId().equals(timeId);

        int golsTime = mandante ? (partida.getGolsMandante() != null ? partida.getGolsMandante() : 0) : (partida.getGolsVisitante() != null ? partida.getGolsVisitante() : 0);
        int golsAdversario = mandante ? (partida.getGolsVisitante() != null ? partida.getGolsVisitante() : 0) : (partida.getGolsMandante() != null ? partida.getGolsMandante() : 0);

        if (golsTime > golsAdversario) return "V";
        if (golsTime < golsAdversario) return "D";
        return "E";
    }
}
