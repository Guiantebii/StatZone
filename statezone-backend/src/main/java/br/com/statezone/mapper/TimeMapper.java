package br.com.statezone.mapper;

import br.com.statezone.dto.TimeRequestDto;
import br.com.statezone.dto.TimeResponseDto;
import br.com.statezone.model.Time;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TimeMapper {

    Time toEntity(TimeRequestDto dto);

    TimeResponseDto toDto(Time entity);
    void updateTimeFromDto(TimeRequestDto dto, @MappingTarget Time entity);
}