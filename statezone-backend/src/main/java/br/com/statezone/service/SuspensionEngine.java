package br.com.statezone.service;

import br.com.statezone.enums.TipoEvento;
import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.Partida;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuspensionEngine {

    private final SuspensaoService suspensaoService;

    public void process(Partida partida) {
        if (partida.getEventos() == null) return;

        for (EventoPartida e : partida.getEventos()) {
            if (e == null || e.getTipoEvento() == null || e.isAnulado()) {
                continue;
            }

            suspensaoService.registrarEventoDisciplinar(
                    e.getJogador(),
                    partida,
                    e.getTipoEvento()
            );
        }
    }
}
