package br.com.statezone.controller;

import br.com.statezone.dto.escalacao.EscalacaoPartidaListResponseDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaRequestDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaResponseDto;
import br.com.statezone.service.EscalacaoPartidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/partidas")
@RequiredArgsConstructor
public class EscalacaoPartidaController {

    private final EscalacaoPartidaService escalacaoPartidaService;

    @PostMapping("/{id}/escalacao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EscalacaoPartidaResponseDto> adicionarJogador(
            @PathVariable Long id,
            @RequestBody @Valid EscalacaoPartidaRequestDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(escalacaoPartidaService.adicionarJogador(id, dto));
    }

    @GetMapping("/{id}/escalacao")
    public ResponseEntity<EscalacaoPartidaListResponseDto> buscarEscalacao(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(escalacaoPartidaService.buscarEscalacao(id));
    }
}