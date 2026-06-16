package br.com.statezone.mapper;

import br.com.statezone.dto.eliminatoria.GrupoResponseDto;
import br.com.statezone.dto.time.TimeResumoDto;
import br.com.statezone.model.Grupo;
import br.com.statezone.model.Time;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GrupoMapper {

    @Mapping(source = "campeonato.id", target = "campeonatoId")
    @Mapping(source = "campeonato.nome", target = "campeonatoNome")
    @Mapping(source = "times", target = "times")
    GrupoResponseDto toDto(Grupo entity);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "sigla", target = "sigla")
    @Mapping(source = "escudoUrl", target = "escudoUrl")
    TimeResumoDto toTimeResumoDto(Time time);
}