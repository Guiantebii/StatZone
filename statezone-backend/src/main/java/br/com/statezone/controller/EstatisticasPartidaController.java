package br.com.statezone.controller;


import br.com.statezone.dto.estatisticasJogador.EstatisticasPartidaResponseDto;
import br.com.statezone.service.EstatisticasPartidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estatisticas")
@RequiredArgsConstructor
public class EstatisticasPartidaController {

    private final EstatisticasPartidaService estatisticasPartidaService;

    @GetMapping("/{partidaId}")
    public EstatisticasPartidaResponseDto get(@PathVariable Long partidaId) {
        return estatisticasPartidaService.gerar(partidaId);
    }
}