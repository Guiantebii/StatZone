package br.com.statezone.mapper;

import br.com.statezone.dto.jogador.JogadorRequestDto;
import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.model.Jogador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface JogadorMapper {

    @Mapping(source = "time.id", target = "timeId")
    @Mapping(source = "time.nome", target = "nomeTime")
    JogadorResponseDto toDto(Jogador entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "apiFootballId", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    Jogador toEntity(JogadorRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "time", ignore = true)
    @Mapping(target = "apiFootballId", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    void updateJogadorFromDto(JogadorRequestDto dto,
                                @MappingTarget Jogador entity);
}
