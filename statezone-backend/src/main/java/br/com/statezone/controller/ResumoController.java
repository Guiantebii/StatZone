package br.com.statezone.controller;

import br.com.statezone.dto.resumo.ResumoResponse;
import br.com.statezone.enums.StatusCampeonato;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.TimeRepository;
import br.com.statezone.service.helper.CampeonatoAccessHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResumoController {

    private final CampeonatoRepository campeonatoRepository;
    private final TimeRepository timeRepository;
    private final JogadorRepository jogadorRepository;
    private final PartidaRepository partidaRepository;
    private final CampeonatoAccessHelper campeonatoAccessHelper;

    @GetMapping("/resumo")
    public ResumoResponse resumo() {
        boolean incluirRascunho = campeonatoAccessHelper.podeVerRascunho();
        long campeonatos = incluirRascunho
                ? campeonatoRepository.count()
                : campeonatoRepository.countByStatus(StatusCampeonato.ATIVO);
        return new ResumoResponse(
                campeonatos,
                timeRepository.count(),
                jogadorRepository.count(),
                partidaRepository.contarVisiveis(incluirRascunho),
                campeonatoRepository.primeiroIdVisivel(incluirRascunho)
        );
    }
}
