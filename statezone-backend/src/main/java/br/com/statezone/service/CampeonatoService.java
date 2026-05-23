package br.com.statezone.service;

import br.com.statezone.dto.CampeonatoRequestDto;
import br.com.statezone.dto.CampeonatoResponseDto;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.CampeonatoMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.repository.CampeonatoRepository;
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


}

