package br.com.statezone.mapper;

import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.model.Partida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PartidaMapper {

    @Mapping(source = "campeonato.id", target = "campeonatoId")
    @Mapping(source = "campeonato.nome", target = "campeonatoNome")

    @Mapping(source = "timeMandante.id", target = "timeMandanteId")
    @Mapping(source = "timeMandante.nome", target = "timeMandanteNome")

    @Mapping(source = "timeVisitante.id", target = "timeVisitanteId")
    @Mapping(source = "timeVisitante.nome", target = "timeVisitanteNome")
    PartidaResponseDto toDto(Partida entity);

    @Mapping(target = "campeonato", ignore = true)
    @Mapping(target = "timeMandante", ignore = true)
    @Mapping(target = "timeVisitante", ignore = true)

    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "estatisticas", ignore = true)

    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    Partida toEntity(PartidaRequestDto dto);


    @Mapping(target = "id", ignore = true)

    @Mapping(target = "campeonato", ignore = true)
    @Mapping(target = "timeMandante", ignore = true)
    @Mapping(target = "timeVisitante", ignore = true)

    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "estatisticas", ignore = true)

    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    void updatePartidaFromDto(
            PartidaRequestDto dto,
            @MappingTarget Partida entity
    );
}
