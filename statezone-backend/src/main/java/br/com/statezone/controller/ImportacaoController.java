package br.com.statezone.controller;

import br.com.statezone.service.ApiFootballImportService;
import br.com.statezone.service.ApiFootballJogadorImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/importacao")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ImportacaoController {

    private final ApiFootballImportService apiFootballImportService;
    private final ApiFootballJogadorImportService jogadorImportService;

    @PostMapping("/times")
    public ResponseEntity<String> importarTimes() {
        apiFootballImportService.importarTimesBrasileirao();
        return ResponseEntity.ok("Times importados com sucesso!");
    }

    @PostMapping("/jogadores/{timeId}")
    public ResponseEntity<String> importarJogadores(@PathVariable Long timeId) {
        jogadorImportService.importarJogadores(timeId);
        return ResponseEntity.ok("Jogadores importados!");
    }

    @PostMapping("/importar-jogadores-todos")
    public ResponseEntity<Void> importarJogadoresTodos() {
        jogadorImportService.importarJogadoresTodosTimes();
        return ResponseEntity.ok().build();
    }
}
