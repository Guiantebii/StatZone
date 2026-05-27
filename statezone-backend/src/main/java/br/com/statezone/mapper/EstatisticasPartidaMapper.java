package br.com.statezone.mapper;

import br.com.statezone.dto.EstatisticasPartidaResponseDto;
import br.com.statezone.model.EstatisticasPartida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EstatisticasPartidaMapper {

    @Mapping(source = "partida.id", target = "partidaId")
    EstatisticasPartidaResponseDto toDto(EstatisticasPartida entity);

    default Integer safe(Integer v) {
        return v == null ? 0 : v;
    }
}