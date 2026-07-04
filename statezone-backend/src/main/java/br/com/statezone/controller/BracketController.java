package br.com.statezone.controller;

import br.com.statezone.dto.eliminatoria.*;
import br.com.statezone.service.BracketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/campeonatos/{campeonatoId}")
@RequiredArgsConstructor
public class BracketController {

    private final BracketService bracketService;

    @PostMapping("/fases")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FaseEliminatoriaResponseDto> criarFase(
            @PathVariable Long campeonatoId,
            @RequestBody @Valid FaseEliminatoriaRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bracketService.criarFase(campeonatoId, dto));
    }

    @GetMapping("/fases")
    public ResponseEntity<List<FaseEliminatoriaResponseDto>> listarFases(
            @PathVariable Long campeonatoId
    ) {
        return ResponseEntity.ok(bracketService.listarFases(campeonatoId));
    }

    @PostMapping("/fases/{faseId}/gerar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> gerarPrimeiraFase(
            @PathVariable Long campeonatoId,
            @PathVariable Long faseId,
            @RequestParam(defaultValue = "2") int vagasPorGrupo
    ) {
        bracketService.gerarPrimeiraFase(campeonatoId, faseId, vagasPorGrupo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/confrontos")
    public ResponseEntity<List<ConfrontoEliminatorioResponseDto>> listarConfrontos(
            @PathVariable Long campeonatoId
    ) {
        return ResponseEntity.ok(bracketService.listarConfrontos(campeonatoId));
    }

    @PostMapping("/confrontos/{confrontoId}/encerrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FaseEliminatoriaResponseDto> encerrarConfronto(
            @PathVariable Long campeonatoId,
            @PathVariable Long confrontoId
    ) {
        return ResponseEntity.ok(
                bracketService.encerraConfronto(campeonatoId, confrontoId)
        );
    }
}