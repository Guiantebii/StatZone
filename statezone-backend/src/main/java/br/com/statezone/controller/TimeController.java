package br.com.statezone.controller;

import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.dto.time.TimeEstatisticasResponseDto;
import br.com.statezone.dto.time.TimePartidasResponseDto;
import br.com.statezone.dto.time.TimeRequestDto;
import br.com.statezone.dto.time.TimeResponseDto;
import br.com.statezone.dto.time.UltimasPartidasTimeResponseDto;
import br.com.statezone.service.ApiFootballImportService;
import br.com.statezone.service.TimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("times")
@RequiredArgsConstructor
public class TimeController {
    private final TimeService timeService;
    private final ApiFootballImportService apiFootballImportService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<List<TimeResponseDto>> listarTimes(
            @RequestParam(required = false, defaultValue = "") String nome,
            @RequestParam(required = false, defaultValue = "0") int pagina,
            @RequestParam(required = false, defaultValue = "50") int tamanho
    ) {
        if (!nome.isBlank()) {
            return ResponseEntity.ok(timeService.buscarPorNome(nome));
        }
        var pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.ASC, "nome"));
        return ResponseEntity.ok(timeService.listarTodosTimes(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeResponseDto> obterTimePorId(@PathVariable Long id){
        return ResponseEntity.ok(timeService.obterTimePorId(id));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimeResponseDto> atualizarTime(
            @RequestBody @Valid TimeRequestDto dto,
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                timeService.atualizarTime(dto, id)
        );
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/{id}/estatisticas")
    public ResponseEntity<TimeEstatisticasResponseDto> obterEstatisticas(@PathVariable Long id){
        return ResponseEntity.ok(timeService.obterEstatisticas(id));
    }

    @GetMapping("/{id}/partidas")
    public ResponseEntity<TimePartidasResponseDto> obterPartidas(@PathVariable Long id){
        return ResponseEntity.ok(timeService.obterPartidas(id));
    }
}
