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
    @Mapping(source = "timeMandante.escudoUrl", target = "escudoMandante")

    @Mapping(source = "timeVisitante.id", target = "timeVisitanteId")
    @Mapping(source = "timeVisitante.nome", target = "timeVisitanteNome")
    @Mapping(source = "timeVisitante.escudoUrl", target = "escudoVisitante")

    @Mapping(source = "formacaoMandante", target = "formacaoMandante")
    @Mapping(source = "formacaoVisitante", target = "formacaoVisitante")
    @Mapping(source = "golsPenaltisMandante", target = "golsPenaltisMandante")
    @Mapping(source = "golsPenaltisVisitante", target = "golsPenaltisVisitante")
    @Mapping(source = "grupo.id", target = "grupoId")
    @Mapping(source = "faseEliminatoria.id", target = "faseEliminatoriaId")
    PartidaResponseDto toDto(Partida entity);

    @Mapping(target = "id", ignore = true)

    @Mapping(target = "campeonato", ignore = true)
    @Mapping(target = "timeMandante", ignore = true)
    @Mapping(target = "timeVisitante", ignore = true)

    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "estatisticas", ignore = true)

    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)

    @Mapping(target = "golsPenaltisMandante", ignore = true)
    @Mapping(target = "golsPenaltisVisitante", ignore = true)
    @Mapping(target = "grupo", ignore = true)
    @Mapping(target = "apiFootballId", ignore = true)
    @Mapping(target = "faseEliminatoria", ignore = true)
    @Mapping(target = "formacaoMandante", ignore = true)
    @Mapping(target = "formacaoVisitante", ignore = true)
    Partida toEntity(PartidaRequestDto dto);


    @Mapping(target = "id", ignore = true)

    @Mapping(target = "campeonato", ignore = true)
    @Mapping(target = "timeMandante", ignore = true)
    @Mapping(target = "timeVisitante", ignore = true)

    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "estatisticas", ignore = true)

    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)

    @Mapping(target = "golsPenaltisMandante", ignore = true)
    @Mapping(target = "golsPenaltisVisitante", ignore = true)
    @Mapping(target = "grupo", ignore = true)
    @Mapping(target = "apiFootballId", ignore = true)
    @Mapping(target = "faseEliminatoria", ignore = true)
    @Mapping(target = "formacaoMandante", ignore = true)
    @Mapping(target = "formacaoVisitante", ignore = true)
    void updatePartidaFromDto(
            PartidaRequestDto dto,
            @MappingTarget Partida entity
    );
}
