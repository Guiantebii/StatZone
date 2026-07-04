package br.com.statezone.controller;

import br.com.statezone.dto.classificacao.ClassificacaoResponseDto;
import br.com.statezone.dto.eliminatoria.GrupoRequestDto;
import br.com.statezone.dto.eliminatoria.GrupoResponseDto;
import br.com.statezone.service.ClassificacaoService;
import br.com.statezone.service.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campeonatos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;
    private final ClassificacaoService classificacaoService;

    @PostMapping("/{campeonatoId}/grupos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GrupoResponseDto> criarGrupo(
            @PathVariable Long campeonatoId,
            @RequestBody @Valid GrupoRequestDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.criarGrupo(campeonatoId, dto));
    }

    @GetMapping("/{campeonatoId}/grupos")
    public ResponseEntity<List<GrupoResponseDto>> listarGrupos(
            @PathVariable Long campeonatoId
    ) {
        return ResponseEntity.ok(grupoService.listarGrupos(campeonatoId));
    }

    @GetMapping("/{campeonatoId}/grupos/{grupoId}")
    public ResponseEntity<GrupoResponseDto> buscarPorId(
            @PathVariable Long campeonatoId,
            @PathVariable Long grupoId
    ) {
        return ResponseEntity.ok(grupoService.buscarPorId(campeonatoId, grupoId));
    }

    @PostMapping("/{campeonatoId}/grupos/{grupoId}/times/{timeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GrupoResponseDto> adicionarTime(
            @PathVariable Long campeonatoId,
            @PathVariable Long grupoId,
            @PathVariable Long timeId
    ) {
        return ResponseEntity.ok(grupoService.adicionarTime(campeonatoId, grupoId, timeId));
    }

    @PostMapping("/{campeonatoId}/grupos/{grupoId}/fixtures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> gerarFixtures(
            @PathVariable Long campeonatoId,
            @PathVariable Long grupoId
    ) {
        grupoService.gerarFixturesPorGrupo(campeonatoId, grupoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{campeonatoId}/grupos/{grupoId}/classificacao")
    public ResponseEntity<List<ClassificacaoResponseDto>> classificacaoDoGrupo(
            @PathVariable Long campeonatoId,
            @PathVariable Long grupoId
    ) {
        return ResponseEntity.ok(classificacaoService.gerarClassificacaoPorGrupo(grupoId));
    }
}