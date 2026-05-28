package br.com.statezone.controller;

import br.com.statezone.dto.*;
import br.com.statezone.service.CampeonatoService;
import br.com.statezone.service.ClassificacaoService;
import br.com.statezone.service.EstatisticasJogadorService;
import br.com.statezone.service.FixtureGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/campeonatos")
@RequiredArgsConstructor
public class CampeonatoController {
    private final CampeonatoService campeonatoService;
    private final ClassificacaoService classificacaoService;
    private final FixtureGeneratorService fixtureGeneratorService;
    private final EstatisticasJogadorService estatisticasJogadorService;

        @PostMapping
        public ResponseEntity<CampeonatoResponseDto> criarCampeonato(@RequestBody @Valid CampeonatoRequestDto dto){
            CampeonatoResponseDto response = campeonatoService.criarCampeonato(dto);

            URI uri = URI.create("/campeonatos/" + response.id());

            return ResponseEntity
                    .created(uri)
                    .body(response);
        }
    @GetMapping
    public ResponseEntity<List<CampeonatoResponseDto>> listarCampeonatos(){
        return ResponseEntity.ok(campeonatoService.listarTodosCampeonatos());
    }
    @GetMapping("/{id}")
    public ResponseEntity<CampeonatoResponseDto> obterCampeonatoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(campeonatoService.obterCampeonatoPorId(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<CampeonatoResponseDto> atualizarCampeonato(@RequestBody @Valid CampeonatoRequestDto dto,@PathVariable Long id) {
        return ResponseEntity.ok(campeonatoService.atualizarCampeonato(dto,id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCampeonato(@PathVariable Long id) {
        campeonatoService.deletarCampeonato(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{campeonatoId}/times/{timeId}")
    public ResponseEntity<Void> adicionarTime(
            @PathVariable Long campeonatoId,
            @PathVariable Long timeId
    ) {
        campeonatoService.adicionarTime(campeonatoId, timeId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{campeonatoId}/classificacao")
    public ResponseEntity<List<ClassificacaoResponseDto>>
    gerarClassificacao(
            @PathVariable Long campeonatoId
    ) {

        List<ClassificacaoResponseDto> response =
                classificacaoService.gerarClassificacao(
                        campeonatoId
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/fixtures")
    public ResponseEntity<List<PartidaResponseDto>> gerarFixtures(@PathVariable Long id) {

        List<PartidaResponseDto> response =
                fixtureGeneratorService.gerarPartida(id);

        URI uri = URI.create("/campeonatos/" + id + "/partidas");

        return ResponseEntity
                .created(uri)
                .body(response);
    }

    @GetMapping("/{id}/partidas")
    public ResponseEntity<List<PartidaResponseDto>> listarPartidas(@PathVariable Long id) {
        return ResponseEntity.ok(campeonatoService.listarPartidas(id));
    }
    @GetMapping("/{id}/artilharia")
    public ResponseEntity<List<ArtilhariaResponseDto>> artilharia(@PathVariable Long id) {
        return ResponseEntity.ok(estatisticasJogadorService.artilharia(id));
    }
}
