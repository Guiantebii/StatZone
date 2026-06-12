package br.com.statezone.mapper;

import br.com.statezone.dto.escalacao.EscalacaoPartidaResponseDto;
import br.com.statezone.model.EscalacaoPartida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EscalacaoPartidaMapper {

    @Mapping(source = "jogador.id", target = "jogadorId")
    @Mapping(source = "jogador.nome", target = "nomeJogador")
    @Mapping(source = "jogador.fotoUrl", target = "fotoUrl")
    @Mapping(source = "jogador.time.nome", target = "nomeTime")
    @Mapping(source = "jogador.time.escudoUrl", target = "escudoTime")
    EscalacaoPartidaResponseDto toDto(EscalacaoPartida entity);
}