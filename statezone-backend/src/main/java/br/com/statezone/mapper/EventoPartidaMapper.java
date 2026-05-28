package br.com.statezone.mapper;

import br.com.statezone.dto.EventoPartidaRequestDto;
import br.com.statezone.dto.EventoPartidaResponseDto;
import br.com.statezone.model.EventoPartida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventoPartidaMapper {

    EventoPartida toEntity(EventoPartidaRequestDto dto);

    @Mapping(target = "partidaId",
            source = "partida.id")

    @Mapping(target = "jogadorId",
            source = "jogador.id")

    @Mapping(target = "nomeJogador",
            source = "jogador.nome")

    @Mapping(target = "nomeTime",
            source = "jogador.time.nome")

    @Mapping(target = "assistenteId",
            source = "assistente.id")

    @Mapping(target = "nomeAssistente",
            source = "assistente.nome")
    EventoPartidaResponseDto toDto(EventoPartida entity);
}