package br.com.statezone.controller;

import br.com.statezone.dto.partida.FormacaoUpdateRequest;
import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.dto.partida.PenaltisRequestDto;
import br.com.statezone.service.EventoPartidaService;
import br.com.statezone.service.PartidaLifecycleService;
import br.com.statezone.service.PartidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/partidas")
@RequiredArgsConstructor
public class PartidaController {
    private final PartidaService partidaService;
    private final PartidaLifecycleService partidaLifecycleService;
    private final EventoPartidaService eventoPartidaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> criarPartida(@RequestBody @Valid PartidaRequestDto dto){
        PartidaResponseDto response = partidaService.criar(dto);
        URI uri = URI.create("/partidas/" + response.id());

        return ResponseEntity
                .created(uri)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PartidaResponseDto>> listarTodasPartidas(
            @RequestParam(required = false, defaultValue = "0") int pagina,
            @RequestParam(required = false, defaultValue = "20") int tamanho
    ){
        Pageable pageable = PageRequest.of(pagina, tamanho,
                Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id")));
        return ResponseEntity.ok(partidaService.listarTodas(pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<PartidaResponseDto> obterPartidaPorId(@PathVariable Long id){
        return ResponseEntity.ok(partidaService.buscarPorId(id));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> atualizarPartida(@RequestBody @Valid PartidaRequestDto dto,@PathVariable Long id){
        return ResponseEntity.ok(partidaService.atualizar(id, dto));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarPartida(@PathVariable Long id){
        partidaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.iniciar(id));
    }
    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> encerrar(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.encerrar(id));
    }

    @PostMapping("/{id}/intervalo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> intervalo(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.intervalo(id));
    }

    @PostMapping("/{id}/segundo-tempo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> iniciarSegundoTempo(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.iniciarSegundoTempo(id));
    }

    @PostMapping("/{id}/adiar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> adiar(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.adiar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.cancelar(id));
    }

    @PatchMapping("/{id}/formacao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> atualizarFormacao(
            @PathVariable Long id,
            @RequestBody @Valid FormacaoUpdateRequest dto
    ) {
        return ResponseEntity.ok(partidaService.atualizarFormacao(id, dto));
    }

    @PostMapping("/{id}/wo-mandante")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> woMandante(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.woMandante(id));
    }

    @PostMapping("/{id}/wo-visitante")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> woVisitante(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.woVisitante(id));
    }

    @PostMapping("/{id}/prorrogacao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> iniciarProrrogacao(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.iniciarProrrogacao(id));
    }

    @PostMapping("/{id}/encerrar-prorrogacao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> encerrarProrrogacao(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.encerrarProrrogacao(id));
    }

    @PostMapping("/{id}/penaltis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> iniciarPenaltis(@PathVariable Long id) {
        return ResponseEntity.ok(partidaLifecycleService.iniciarPenaltis(id));
    }

    @PostMapping("/{id}/encerrar-penaltis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartidaResponseDto> encerrarComPenaltis(
            @PathVariable Long id,
            @RequestBody @Valid PenaltisRequestDto dto
    ) {
        return ResponseEntity.ok(partidaLifecycleService.encerrarComPenaltis(
                id,
                dto.golsPenaltisMandante(),
                dto.golsPenaltisVisitante()
        ));
    }

}
