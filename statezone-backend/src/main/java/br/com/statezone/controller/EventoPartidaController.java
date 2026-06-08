package br.com.statezone.controller;

import br.com.statezone.dto.eventoPartida.EventoPartidaRequestDto;
import br.com.statezone.dto.eventoPartida.EventoPartidaResponseDto;
import br.com.statezone.dto.eventoPartida.EventoTimelineResponseDto;
import br.com.statezone.service.EventoPartidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/partidas")
@RequiredArgsConstructor
public class EventoPartidaController {
    private final EventoPartidaService eventoPartidaService;

    @PostMapping("/{partidaId}/eventos")
    public ResponseEntity<EventoPartidaResponseDto> registrarEvento(@Valid @RequestBody EventoPartidaRequestDto dto, @PathVariable Long partidaId){
        EventoPartidaResponseDto response =
                eventoPartidaService
                        .registrarEvento(dto, partidaId);

        URI uri = URI.create(
                "/partidas/" +
                        partidaId +
                        "/eventos/" +
                        response.id()
        );

        return ResponseEntity
                .created(uri)
                .body(response);

    }

    @GetMapping("/{partidaId}/eventos")
    public ResponseEntity<List<EventoPartidaResponseDto>> listarEventos(@PathVariable Long partidaId){
        List<EventoPartidaResponseDto> response =
                eventoPartidaService.listarEventosPorPartida(partidaId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{partidaId}/timeline")
    public ResponseEntity<List<EventoTimelineResponseDto>> timeline(
            @PathVariable Long partidaId
    ) {
        return ResponseEntity.ok(
                eventoPartidaService.buscarTimeline(partidaId)
        );
    }

}
