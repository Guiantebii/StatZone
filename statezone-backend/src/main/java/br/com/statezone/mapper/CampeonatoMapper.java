package br.com.statezone.mapper;

import br.com.statezone.dto.CampeonatoRequestDto;
import br.com.statezone.dto.CampeonatoResponseDto;
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

    Campeonato toEntity(CampeonatoRequestDto dto);

    void updateCampeonatoFromDto(CampeonatoRequestDto dto,
                                 @MappingTarget Campeonato entity);

    default List<Long> mapTimes(List<Time> times) {
        if (times == null) return null;

        return times.stream()
                .map(br.com.statezone.model.Time::getId)
                .toList();
    }
}