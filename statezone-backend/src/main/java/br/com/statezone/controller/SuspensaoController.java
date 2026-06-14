package br.com.statezone.controller;

import br.com.statezone.dto.suspensao.SuspensaoResponseDto;
import br.com.statezone.service.SuspensaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/campeonatos")
@RequiredArgsConstructor
public class SuspensaoController {

    private final SuspensaoService suspensaoService;

    @GetMapping("/{id}/suspensoes")
    public ResponseEntity<List<SuspensaoResponseDto>> listarSuspensoesProximaRodada(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                suspensaoService.listarSuspensoesProximaRodada(id)
        );
    }

    @GetMapping("/{id}/suspensoes/rodada/{rodada}")
    public ResponseEntity<List<SuspensaoResponseDto>> listarSuspensoesPorRodada(
            @PathVariable Long id,
            @PathVariable Integer rodada
    ) {
        return ResponseEntity.ok(
                suspensaoService.listarSuspensoesPorRodada(id, rodada)
        );
    }
}