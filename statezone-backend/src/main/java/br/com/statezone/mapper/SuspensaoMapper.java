package br.com.statezone.mapper;

import br.com.statezone.dto.suspensao.SuspensaoResponseDto;
import br.com.statezone.model.Suspensao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SuspensaoMapper {

    @Mapping(source = "jogador.id", target = "jogadorId")
    @Mapping(source = "jogador.nome", target = "nomeJogador")
    @Mapping(source = "jogador.fotoUrl", target = "fotoUrl")
    @Mapping(source = "jogador.time.nome", target = "nomeTime")
    @Mapping(source = "jogador.time.escudoUrl", target = "escudoTime")
    SuspensaoResponseDto toDto(Suspensao entity);
}