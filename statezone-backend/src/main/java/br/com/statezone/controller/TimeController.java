package br.com.statezone.controller;

import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.dto.time.TimeRequestDto;
import br.com.statezone.dto.time.TimeResponseDto;
import br.com.statezone.dto.time.UltimasPartidasTimeResponseDto;
import br.com.statezone.service.TimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("times")
@RequiredArgsConstructor
public class TimeController {
    private final TimeService timeService;

    @PostMapping
    public ResponseEntity<TimeResponseDto> criarTime(
            @RequestBody @Valid TimeRequestDto dto
    ) {

        TimeResponseDto response = timeService.criar(dto);

        URI uri = URI.create("/times/" + response.id());

        return ResponseEntity
                .created(uri)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<List<TimeResponseDto>> listarTimes(){
        return ResponseEntity.ok(timeService.listarTodosTimes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeResponseDto> obterTimePorId(@PathVariable Long id){
        return ResponseEntity.ok(timeService.obterTimePorId(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<TimeResponseDto> atualizarTime(
            @RequestBody @Valid TimeRequestDto dto,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                timeService.atualizarTime(dto, id)
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTime(
            @PathVariable Long id
    ) {

        timeService.deletarTime(id);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/jogadores")
    public ResponseEntity<List<JogadorResponseDto>> listarJogadores(@PathVariable Long id) {
        return ResponseEntity.ok(timeService.listarJogadoresPorTime(id));
    }

    @GetMapping("/{id}/forma")
    public ResponseEntity<UltimasPartidasTimeResponseDto> ultimas5Partidas(@PathVariable Long id){
        return ResponseEntity.ok(timeService.ultimas5Partidas(id));
    }
}
