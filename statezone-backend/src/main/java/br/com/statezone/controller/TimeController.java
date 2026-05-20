package br.com.statezone.controller;

import br.com.statezone.dto.TimeRequestDto;
import br.com.statezone.dto.TimeResponseDto;
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
}
