package br.com.statezone.mapper;

import br.com.statezone.dto.time.TimeRequestDto;
import br.com.statezone.dto.time.TimeResponseDto;
import br.com.statezone.model.Time;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TimeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seed", ignore = true)
    @Mapping(target = "apiFootballId", ignore = true)
    @Mapping(target = "jogadores", ignore = true)
    @Mapping(target = "partidasMandante", ignore = true)
    @Mapping(target = "partidasVisitante", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    Time toEntity(TimeRequestDto dto);

    TimeResponseDto toDto(Time entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seed", ignore = true)
    @Mapping(target = "apiFootballId", ignore = true)
    @Mapping(target = "jogadores", ignore = true)
    @Mapping(target = "partidasMandante", ignore = true)
    @Mapping(target = "partidasVisitante", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    void updateTimeFromDto(TimeRequestDto dto, @MappingTarget Time entity);
}