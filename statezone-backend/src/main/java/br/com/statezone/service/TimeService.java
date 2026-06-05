package br.com.statezone.service;

import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.dto.time.TimeRequestDto;
import br.com.statezone.dto.time.TimeResponseDto;
import br.com.statezone.dto.time.UltimasPartidasTimeResponseDto;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.JogadorMapper;
import br.com.statezone.mapper.TimeMapper;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.TimeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

        Time time = timeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Time com id " + id + " não encontrado"
                        )
                );

        timeRepository.delete(time);
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

    private String calcularResultado(Partida partida, Long timeId) {
        boolean mandante = partida.getTimeMandante().getId().equals(timeId);

        int golsTime = mandante ? partida.getGolsMandante() : partida.getGolsVisitante();
        int golsAdversario = mandante ? partida.getGolsVisitante() : partida.getGolsMandante();

        if (golsTime > golsAdversario) return "V";
        if (golsTime < golsAdversario) return "D";
        return "E";
    }
}
