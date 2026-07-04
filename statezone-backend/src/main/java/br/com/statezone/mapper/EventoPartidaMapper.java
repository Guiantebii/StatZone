package br.com.statezone.mapper;

import br.com.statezone.dto.eventoPartida.EventoPartidaRequestDto;
import br.com.statezone.dto.eventoPartida.EventoPartidaResponseDto;
import br.com.statezone.dto.eventoPartida.EventoTimelineResponseDto;
import br.com.statezone.model.EventoPartida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventoPartidaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventoRelacionado", ignore = true)
    @Mapping(target = "anulado", ignore = true)
    @Mapping(target = "partida", ignore = true)
    @Mapping(target = "jogador", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "jogadorSecundario", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
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

    @Mapping(target = "timeId", expression = "java(entity.getTime() != null ? entity.getTime().getId() : null)")
    @Mapping(target = "nomeTime", expression = "java(entity.getTime() != null ? entity.getTime().getNome() : null)")

    @Mapping(target = "jogadorId", expression = "java(entity.getJogador() != null ? entity.getJogador().getId() : null)")
    @Mapping(target = "jogador", expression = "java(entity.getJogador() != null ? entity.getJogador().getNome() : null)")

    @Mapping(target = "jogadorSecundarioId", expression = "java(entity.getJogadorSecundario() != null ? entity.getJogadorSecundario().getId() : null)")
    @Mapping(target = "jogadorSecundario", expression = "java(entity.getJogadorSecundario() != null ? entity.getJogadorSecundario().getNome() : null)")
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