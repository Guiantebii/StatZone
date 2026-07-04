package br.com.statezone.mapper;

import br.com.statezone.dto.campeonato.CampeonatoRequestDto;
import br.com.statezone.dto.campeonato.CampeonatoResponseDto;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Time;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CampeonatoMapper {

    @Mapping(target = "timesIds", source = "times")
    CampeonatoResponseDto toDto(Campeonato entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiFootballId", ignore = true)
    @Mapping(target = "partidas", ignore = true)
    @Mapping(target = "times", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    Campeonato toEntity(CampeonatoRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiFootballId", ignore = true)
    @Mapping(target = "partidas", ignore = true)
    @Mapping(target = "times", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    void updateCampeonatoFromDto(CampeonatoRequestDto dto,
                                 @MappingTarget Campeonato entity);

    default List<Long> mapTimes(List<Time> times) {
        if (times == null) return List.of();

        return times.stream()
                .map(br.com.statezone.model.Time::getId)
                .toList();
    }
}