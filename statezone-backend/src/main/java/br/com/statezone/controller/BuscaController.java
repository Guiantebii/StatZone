package br.com.statezone.controller;

import br.com.statezone.dto.busca.BuscaResponse;
import br.com.statezone.dto.campeonato.CampeonatoResponseDto;
import br.com.statezone.enums.StatusCampeonato;
import br.com.statezone.mapper.CampeonatoMapper;
import br.com.statezone.mapper.JogadorMapper;
import br.com.statezone.mapper.TimeMapper;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.TimeRepository;
import br.com.statezone.service.helper.CampeonatoAccessHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BuscaController {

    private static final int LIMITE = 8;

    private final TimeRepository timeRepository;
    private final JogadorRepository jogadorRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final TimeMapper timeMapper;
    private final JogadorMapper jogadorMapper;
    private final CampeonatoMapper campeonatoMapper;
    private final CampeonatoAccessHelper campeonatoAccessHelper;

    @GetMapping("/busca")
    public BuscaResponse buscar(@RequestParam(defaultValue = "") String q) {
        String termo = q.trim();
        if (termo.length() < 2) {
            return new BuscaResponse(List.of(), List.of(), List.of());
        }
        boolean incluirRascunho = campeonatoAccessHelper.podeVerRascunho();
        List<CampeonatoResponseDto> campeonatos = campeonatoRepository.findByNomeContainingIgnoreCase(termo).stream()
                .filter(c -> incluirRascunho || c.getStatus() != StatusCampeonato.RASCUNHO)
                .limit(LIMITE)
                .map(campeonatoMapper::toDto)
                .toList();
        return new BuscaResponse(
                timeRepository.findByNomeContainingIgnoreCase(termo).stream().limit(LIMITE).map(timeMapper::toDto).toList(),
                jogadorRepository.findByNomeContainingIgnoreCase(termo).stream().limit(LIMITE).map(jogadorMapper::toDto).toList(),
                campeonatos
        );
    }
}
