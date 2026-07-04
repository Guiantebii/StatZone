package br.com.statezone.controller;

import br.com.statezone.dto.campeonato.CampeonatoRequestDto;
import br.com.statezone.dto.campeonato.CampeonatoResponseDto;
import br.com.statezone.dto.classificacao.ClassificacaoResponseDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.dto.rankings.*;
import br.com.statezone.dto.time.TimeResponseDto;
import br.com.statezone.service.CampeonatoService;
import br.com.statezone.service.ClassificacaoService;
import br.com.statezone.service.EstatisticasJogadorService;
import br.com.statezone.service.FixtureGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampeonatoResponseDto> atualizarCampeonato(@RequestBody @Valid CampeonatoRequestDto dto,@PathVariable Long id) {
        return ResponseEntity.ok(campeonatoService.atualizarCampeonato(dto,id));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarCampeonato(@PathVariable Long id) {
        campeonatoService.deletarCampeonato(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{campeonatoId}/times/{timeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adicionarTime(
            @PathVariable Long campeonatoId,
            @PathVariable Long timeId
    ) {
        campeonatoService.adicionarTime(campeonatoId, timeId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{campeonatoId}/times")
    public ResponseEntity<List<TimeResponseDto>> listarTimesDoCampeonato(@PathVariable Long campeonatoId) {
        return ResponseEntity.ok(campeonatoService.listarTimesDoCampeonato(campeonatoId));
    }
    @GetMapping("/{campeonatoId}/classificacao")
    public ResponseEntity<List<ClassificacaoResponseDto>> gerarClassificacao(
            @PathVariable Long campeonatoId,
            @RequestParam(required = false) Integer turno
    ) {
        if (turno != null) {
            return ResponseEntity.ok(
                    classificacaoService.gerarClassificacaoPorTurno(campeonatoId, turno)
            );
        }
        return ResponseEntity.ok(classificacaoService.gerarClassificacao(campeonatoId));
    }

    @PostMapping("/{id}/fixtures")
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<List<ArtilhariaResponseDto>> artilharia(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho
    ) {
        return ResponseEntity.ok(estatisticasJogadorService.artilharia(id, pagina, tamanho));
    }

    @GetMapping("/{id}/assistencias")
    public ResponseEntity<List<AssistenciaRankingResponseDto>> rankingAssistencias(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho
    ) {
        return ResponseEntity.ok(estatisticasJogadorService.rankingAssistencias(id, pagina, tamanho));
    }

    @GetMapping("/{id}/ranking/cartoes-amarelos")
    public ResponseEntity<List<RankingCartaoAmareloResponseDto>> rankingCartaoAmarelo(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho
    ) {
        return ResponseEntity.ok(estatisticasJogadorService.rankingCartaoAmarelo(id, pagina, tamanho));
    }

    @GetMapping("/{id}/ranking/cartoes-vermelhos")
    public ResponseEntity<List<RankingCartaoVermelhoResponseDto>> rankingCartaoVermelho(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho
    ) {
        return ResponseEntity.ok(estatisticasJogadorService.rankingCartaoVermelho(id, pagina, tamanho));
    }

    @GetMapping("/{campeonatoId}/selecao-do-campeonato")
    public ResponseEntity<List<SelecaoCampeonatoResponseDto>> getSelecao(@PathVariable Long campeonatoId) {
        return ResponseEntity.ok(estatisticasJogadorService.gerarSelecaoDoCampeonato(campeonatoId));
    }


    @GetMapping("/{campeonatoId}/mvp")
    public ResponseEntity<CraqueCampeonatoResponseDto> mvpCampeonato(@PathVariable Long campeonatoId) {
        var mvp = estatisticasJogadorService.mvpCampeonato(campeonatoId);
        return mvp != null ? ResponseEntity.ok(mvp) : ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ranking/goleiros")
    public ResponseEntity<List<RankingGoleiroResponseDto>> rankingGoleiros(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho
    ) {
        return ResponseEntity.ok(estatisticasJogadorService.rankingGoleiros(id, pagina, tamanho));
    }

    @PostMapping("/{id}/reprocessar-estatisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reprocessarEstatisticas(@PathVariable Long id) {
        campeonatoService.reprocessarEstatisticas(id);
        return ResponseEntity.ok().build();
    }

}
