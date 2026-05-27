package br.com.statezone.controller;

import br.com.statezone.dto.PartidaRequestDto;
import br.com.statezone.dto.PartidaResponseDto;
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
        return ResponseEntity.ok(partidaService.atualizar(dto,id));
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


}
