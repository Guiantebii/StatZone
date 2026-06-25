package br.com.statezone.controller;

import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.dto.partida.PenaltisRequestDto;
import br.com.statezone.service.EventoPartidaService;
import br.com.statezone.service.PartidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/partidas")
@RequiredArgsConstructor
public class PartidaController {
    private final PartidaService partidaService;
    private final EventoPartidaService eventoPartidaService;

    @PostMapping
    public ResponseEntity<PartidaResponseDto> criarPartida(@RequestBody @Valid PartidaRequestDto dto){
        PartidaResponseDto response = partidaService.criar(dto);
        URI uri = URI.create("/partidas/" + response.id());

        return ResponseEntity
                .created(uri)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PartidaResponseDto>> listarTodasPartidas(){
        return ResponseEntity.ok(partidaService.listarTodas());
    }
    @GetMapping("/{id}")
    public ResponseEntity<PartidaResponseDto> obterPartidaPorId(@PathVariable Long id){
        return ResponseEntity.ok(partidaService.buscarPorId(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<PartidaResponseDto> atualizarPartida(@RequestBody @Valid PartidaRequestDto dto,@PathVariable Long id){
        return ResponseEntity.ok(partidaService.atualizar(id, dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPartida(@PathVariable Long id){
        partidaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<PartidaResponseDto> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.iniciar(id));
    }
    @PostMapping("/{id}/encerrar")
    public ResponseEntity<PartidaResponseDto> encerrar(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.encerrar(id));
    }

    @PostMapping("/{id}/intervalo")
    public ResponseEntity<PartidaResponseDto> intervalo(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.intervalo(id));
    }

    @PostMapping("/{id}/segundo-tempo")
    public ResponseEntity<PartidaResponseDto> iniciarSegundoTempo(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.iniciarSegundoTempo(id));
    }

    @PostMapping("/{id}/adiar")
    public ResponseEntity<PartidaResponseDto> adiar(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.adiar(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PartidaResponseDto> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.cancelar(id));
    }

    @PostMapping("/{id}/wo-mandante")
    public ResponseEntity<PartidaResponseDto> woMandante(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.woMandante(id));
    }

    @PostMapping("/{id}/wo-visitante")
    public ResponseEntity<PartidaResponseDto> woVisitante(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.woVisitante(id));
    }

    @PostMapping("/{id}/penaltis")
    public ResponseEntity<PartidaResponseDto> iniciarPenaltis(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.iniciarPenaltis(id));
    }

    @PostMapping("/{id}/encerrar-penaltis")
    public ResponseEntity<PartidaResponseDto> encerrarComPenaltis(
            @PathVariable Long id,
            @RequestBody @Valid PenaltisRequestDto dto
    ) {
        return ResponseEntity.ok(partidaService.encerrarComPenaltis(
                id,
                dto.golsPenaltisMandante(),
                dto.golsPenaltisVisitante()
        ));
    }

}
