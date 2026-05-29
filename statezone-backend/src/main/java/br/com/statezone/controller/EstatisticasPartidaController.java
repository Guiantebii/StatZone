package br.com.statezone.controller;


import br.com.statezone.dto.estatisticasJogador.EstatisticasPartidaResponseDto;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.EstatisticasPartidaMapper;
import br.com.statezone.model.EstatisticasPartida;
import br.com.statezone.repository.EstatisticasPartidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estatisticas")
@RequiredArgsConstructor
public class EstatisticasPartidaController {

    private final EstatisticasPartidaRepository repository;
    private final EstatisticasPartidaMapper mapper;

    @GetMapping("/{partidaId}")
    public EstatisticasPartidaResponseDto get(@PathVariable Long partidaId) {

        EstatisticasPartida stats = repository.findByPartidaId(partidaId)
                .orElseThrow(() -> new ResourceNotFoundException("Estatísticas não encontradas"));

        return mapper.toDto(stats);
    }
}