package br.com.statezone.service;

import br.com.statezone.dto.JogadorResponseDto;
import br.com.statezone.dto.TimeRequestDto;
import br.com.statezone.dto.TimeResponseDto;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.JogadorMapper;
import br.com.statezone.mapper.TimeMapper;
import br.com.statezone.model.Time;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.TimeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

}
