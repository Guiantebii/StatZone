package br.com.statezone.service;

import br.com.statezone.dto.partida.PartidaResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartidaWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notificarAtualizacaoPartida(PartidaResponseDto partida) {
        messagingTemplate.convertAndSend(
                "/topic/partidas/" + partida.id(),
                partida
        );
    }
}