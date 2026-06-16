package br.com.statezone.mapper;

import br.com.statezone.dto.eliminatoria.ConfrontoEliminatorioResponseDto;
import br.com.statezone.dto.eliminatoria.FaseEliminatoriaResponseDto;
import br.com.statezone.dto.time.TimeResumoDto;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.FaseEliminatoria;
import br.com.statezone.model.Time;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FaseEliminatoriaMapper {

    @Mapping(source = "campeonato.id", target = "campeonatoId")
    @Mapping(source = "confrontos", target = "confrontos")
    FaseEliminatoriaResponseDto toDto(FaseEliminatoria entity);

    @Mapping(source = "timeA", target = "timeA")
    @Mapping(source = "timeB", target = "timeB")
    @Mapping(source = "partidaIda.id", target = "partidaIdaId")
    @Mapping(source = "partidaVolta.id", target = "partidaVoltaId")
    @Mapping(source = "timeClassificado", target = "timeClassificado")
    ConfrontoEliminatorioResponseDto toConfrontoDto(ConfrontoEliminatorio entity);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "sigla", target = "sigla")
    @Mapping(source = "escudoUrl", target = "escudoUrl")
    TimeResumoDto toTimeResumoDto(Time time);
}