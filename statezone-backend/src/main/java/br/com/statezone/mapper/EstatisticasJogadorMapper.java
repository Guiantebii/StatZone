package br.com.statezone.mapper;

import br.com.statezone.dto.ArtilhariaResponseDto;
import br.com.statezone.dto.EstatisticasJogadorResponseDto;
import br.com.statezone.model.EstatisticasJogador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EstatisticasJogadorMapper {
    @Mapping(source = "jogador.id", target = "jogadorId")
    @Mapping(source = "jogador.nome", target = "nomeJogador")
    @Mapping(source = "jogador.time.nome", target = "nomeTime")
    EstatisticasJogadorResponseDto toDto(EstatisticasJogador entity);



    @Mapping(source = "jogador.id", target = "jogadorId")
    @Mapping(source = "jogador.nome", target = "nomeJogador")
    @Mapping(source = "jogador.time.nome", target = "nomeTime")
    @Mapping(source = "jogador.time.escudoUrl", target = "escudoTime")
    @Mapping(target = "posicao", ignore = true)
    ArtilhariaResponseDto toArtilhariaDto(EstatisticasJogador entity);
}
