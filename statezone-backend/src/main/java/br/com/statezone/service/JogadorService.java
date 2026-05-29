package br.com.statezone.service;

import br.com.statezone.dto.jogador.JogadorRequestDto;
import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.JogadorMapper;
import br.com.statezone.model.Jogador;
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
public class JogadorService {

    private final JogadorRepository jogadorRepository;
    private final JogadorMapper jogadorMapper;
    private final TimeRepository timeRepository;


    public JogadorResponseDto criar(JogadorRequestDto dto) {
        Time time = timeRepository.findById(dto.timeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Time com id " + dto.timeId() + " não encontrado"
                        )
                );
        Jogador entity = jogadorMapper.toEntity(dto);
        entity.setTime(time);
        Jogador salvo = jogadorRepository.save(entity);
        return jogadorMapper.toDto(salvo);
    }

    public List<JogadorResponseDto> listarTodosJogadores() {
        return jogadorRepository.findAll()
                .stream()
                .map(jogadorMapper::toDto)
                .toList();
    }

    public JogadorResponseDto obterJogadorPorId(Long id) {
        Jogador jogador = jogadorRepository.findById(id)
                .orElseThrow(()->
                new ResourceNotFoundException(
                        "Jogador com id " + id + " não encontrado"
                )
        );
        return jogadorMapper.toDto(jogador);
    }

    public JogadorResponseDto atualizarJogador(JogadorRequestDto dto, Long id){
        Jogador jogador = jogadorRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                "Jogador com id " + id + " não encontrado"
                        )
                );
        Time time = timeRepository.findById(dto.timeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Time com id " + dto.timeId() + " não encontrado"
                        )
                );
        jogadorMapper.updateJogadorFromDto(dto, jogador);

        jogador.setTime(time);

        Jogador jogadorAtualizado = jogadorRepository.save(jogador);

        return jogadorMapper.toDto(jogadorAtualizado);
    }

    public void deletarJogador(Long id){
        Jogador jogador = jogadorRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                "Jogador com id " + id + " não encontrado"
                        )
                );
        jogadorRepository.delete(jogador);
    }
}