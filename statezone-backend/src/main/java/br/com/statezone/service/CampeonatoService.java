package br.com.statezone.service;

import br.com.statezone.dto.campeonato.CampeonatoRequestDto;
import br.com.statezone.dto.campeonato.CampeonatoResponseDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.CampeonatoMapper;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Time;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.TimeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CampeonatoService {

    private final CampeonatoRepository campeonatoRepository;
    private final CampeonatoMapper campeonatoMapper;
    private final TimeRepository timeRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMapper partidaMapper;

    public CampeonatoResponseDto criarCampeonato(CampeonatoRequestDto dto){
        Campeonato entity = campeonatoMapper.toEntity(dto);
        Campeonato salvo = campeonatoRepository.save(entity);
        return campeonatoMapper.toDto(salvo);
    }
    public List<CampeonatoResponseDto> listarTodosCampeonatos(){
        return campeonatoRepository.findAll()
                .stream()
                .map(campeonatoMapper::toDto)
                .toList();
    }

    public CampeonatoResponseDto obterCampeonatoPorId(Long id){
        Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Campeonato com id " + id + " não encontrado"));
        return campeonatoMapper.toDto(campeonato);

    }
    public CampeonatoResponseDto atualizarCampeonato(CampeonatoRequestDto dto ,Long id){
        Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Campeonato com id " + id + " não encontrado"));
        campeonatoMapper.updateCampeonatoFromDto(dto,campeonato);
        Campeonato campeonatoAtualizado = campeonatoRepository.save(campeonato);
        return campeonatoMapper.toDto(campeonatoAtualizado);
    }

    public void deletarCampeonato(Long id){
        Campeonato campeonato = campeonatoRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Campeonato com id " + id + " não encontrado"));
        campeonatoRepository.delete(campeonato);
    }

    public void adicionarTime(Long campeonatoId, Long timeId){

        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Campeonato não encontrado")
                );

        Time time = timeRepository.findById(timeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Time não encontrado")
                );

        if (campeonato.getTimes().contains(time)) {
            throw new ConflictException("Time já está no campeonato");
        }

        campeonato.getTimes().add(time);

        campeonatoRepository.save(campeonato);
    }

    public List<PartidaResponseDto> listarPartidas(Long campeonatoId) {

        campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));

        return partidaRepository.findByCampeonatoId(campeonatoId)
                .stream()
                .map(partidaMapper::toDto)
                .toList();
    }


}

