package br.com.statezone.mapper;

import br.com.statezone.dto.eventoPartida.EventoPartidaRequestDto;
import br.com.statezone.dto.eventoPartida.EventoPartidaResponseDto;
import br.com.statezone.dto.eventoPartida.EventoTimelineResponseDto;
import br.com.statezone.model.EventoPartida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventoPartidaMapper {

    EventoPartida toEntity(EventoPartidaRequestDto dto);

    @Mapping(target = "partidaId", source = "partida.id")
    @Mapping(target = "jogadorId", source = "jogador.id")
    @Mapping(target = "nomeJogador", source = "jogador.nome")
    @Mapping(target = "nomeTime", source = "time.nome")
    @Mapping(target = "assistenteId", source = "jogadorSecundario.id")
    @Mapping(target = "nomeAssistente", source = "jogadorSecundario.nome")

    @Mapping(target = "eventoRelacionadoId", source = "eventoRelacionado.id")
    @Mapping(target = "anulado", source = "anulado")

    EventoPartidaResponseDto toDto(EventoPartida entity);

    @Mapping(target = "tipo", source = "tipoEvento")
    @Mapping(target = "tempo", expression = "java(formatarTempo(entity))")

    @Mapping(target = "timeId", source = "time.id")
    @Mapping(target = "nomeTime", source = "time.nome")

    @Mapping(target = "jogadorId", source = "jogador.id")
    @Mapping(target = "jogador", source = "jogador.nome")

    @Mapping(target = "jogadorSecundarioId", source = "jogadorSecundario.id")
    @Mapping(target = "jogadorSecundario", source = "jogadorSecundario.nome")
    EventoTimelineResponseDto toTimelineDto(EventoPartida entity);

    default String formatarTempo(EventoPartida evento) {

        if (evento.getMinutoExtra() == null ||
                evento.getMinutoExtra() == 0) {

            return evento.getMinuto() + "'";
        }

        return evento.getMinuto()
                + "+"
                + evento.getMinutoExtra()
                + "'";
    }
}