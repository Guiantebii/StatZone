package br.com.statezone.mapper;

import br.com.statezone.dto.ClassificacaoResponseDto;
import br.com.statezone.service.helper.ClassificacaoStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassificacaoMapper {
    @Mapping(source = "jogos", target = "partidas")
    ClassificacaoResponseDto toDto(
            ClassificacaoStats stats
    );
}