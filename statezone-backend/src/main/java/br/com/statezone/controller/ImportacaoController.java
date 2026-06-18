package br.com.statezone.controller;


import br.com.statezone.service.ApiFootballImportService;
import br.com.statezone.service.ApiFootballJogadorImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api/importacao")
@RequiredArgsConstructor
public class ImportacaoController {

    private final ApiFootballImportService apiFootballImportService;
    private final ApiFootballJogadorImportService jogadorImportService;
    private final WebClient apiFootballWebClient;

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


    @GetMapping("/teste-fixtures")
    public Object buscarPartidasTeste() {
        return apiFootballWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures")
                        .queryParam("league", 71)
                        .queryParam("season", 2024)
                        .queryParam("round", "Regular Season - 1")
                        .build())
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
}
