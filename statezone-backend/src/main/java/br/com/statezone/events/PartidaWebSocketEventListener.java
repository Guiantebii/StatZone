package br.com.statezone.events;

import br.com.statezone.dto.eventoPartida.EventoTimelineResponseDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.mapper.EventoPartidaMapper;
import br.com.statezone.mapper.PartidaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartidaWebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final EventoPartidaMapper eventoMapper;
    private final PartidaMapper partidaMapper;

    @EventListener
    public void onEventoCriado(EventoPartidaCriadaEvent event) {
        EventoTimelineResponseDto timelineDto = eventoMapper.toTimelineDto(event.evento());
        messagingTemplate.convertAndSend(
                "/topic/partidas/" + event.partida().getId() + "/eventos",
                timelineDto
        );

        PartidaResponseDto partidaDto = partidaMapper.toDto(event.partida());
        messagingTemplate.convertAndSend(
                "/topic/partidas/" + event.partida().getId(),
                partidaDto
        );
    }
}
