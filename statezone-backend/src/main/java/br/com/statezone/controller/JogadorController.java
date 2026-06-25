package br.com.statezone.controller;

import br.com.statezone.dto.estatisticasJogador.EstatisticasJogadorResponseDto;
import br.com.statezone.dto.jogador.JogadorRequestDto;
import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.service.EstatisticasJogadorService;
import br.com.statezone.service.JogadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/jogadores")
@RequiredArgsConstructor
public class JogadorController {
    private final JogadorService jogadorService;
    private final EstatisticasJogadorService estatisticasJogadorService;

    @PostMapping
    public ResponseEntity<JogadorResponseDto> criarJogador(
            @RequestBody
            @Valid
            JogadorRequestDto dto){
        JogadorResponseDto response = jogadorService.criar(dto);

        URI uri = URI.create("/jogadores/" + response.id());

        return ResponseEntity
                .created(uri)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<JogadorResponseDto>> listarJogadores(
            @RequestParam(required = false, defaultValue = "") String nome
    ) {
        if (!nome.isBlank()) {
            return ResponseEntity.ok(jogadorService.buscarPorNome(nome));
        }
        return ResponseEntity.ok(jogadorService.listarTodosJogadores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JogadorResponseDto> obterJogadorPorId(@PathVariable Long id){
        return ResponseEntity.ok(jogadorService.obterJogadorPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JogadorResponseDto> atualizarJogador(@RequestBody @Valid JogadorRequestDto dto, @PathVariable Long id){
       return ResponseEntity.ok(jogadorService.atualizarJogador(dto,id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarJogador(@PathVariable Long id){
        jogadorService.deletarJogador(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/estatisticas")
    public ResponseEntity<EstatisticasJogadorResponseDto> estatisticas (@PathVariable Long id){
        return ResponseEntity.ok(estatisticasJogadorService.buscarPorJogador(id));
    }


}
