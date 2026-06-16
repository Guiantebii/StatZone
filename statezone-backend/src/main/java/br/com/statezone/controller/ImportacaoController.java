package br.com.statezone.controller;

import br.com.statezone.service.ApiFootballImportService;
import br.com.statezone.service.ApiFootballJogadorImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/importacao")
@RequiredArgsConstructor
public class ImportacaoController {

    private final ApiFootballImportService apiFootballImportService;
    private final ApiFootballJogadorImportService jogadorImportService;

    @PostMapping("/times")
    public String importarTimes() {

        apiFootballImportService.importarTimesBrasileirao();

        return "Times importados com sucesso!";
    }
    @PostMapping("/jogadores/{timeId}")
    public String importarJogadores(
            @PathVariable Long timeId
    ) {

        jogadorImportService.importarJogadores(timeId);

        return "Jogadores importados!";
    }

    @PostMapping("/importar-jogadores-todos")
    public ResponseEntity<Void> importarJogadoresTodos() {

        jogadorImportService.importarJogadoresTodosTimes();

        return ResponseEntity.ok().build();
    }
}
