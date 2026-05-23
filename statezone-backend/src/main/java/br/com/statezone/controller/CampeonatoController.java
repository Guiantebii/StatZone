package br.com.statezone.controller;

import br.com.statezone.dto.CampeonatoRequestDto;
import br.com.statezone.dto.CampeonatoResponseDto;
import br.com.statezone.service.CampeonatoService;
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



}
