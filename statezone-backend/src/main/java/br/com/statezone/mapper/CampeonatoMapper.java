package br.com.statezone.mapper;

import br.com.statezone.dto.CampeonatoRequestDto;
import br.com.statezone.dto.CampeonatoResponseDto;
import br.com.statezone.model.Campeonato;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CampeonatoMapper {

    CampeonatoResponseDto toDto(Campeonato entity);

    Campeonato toEntity(CampeonatoRequestDto dto);

    void updateCampeonatoFromDto(CampeonatoRequestDto dto,
                                 @MappingTarget Campeonato entity);
}
