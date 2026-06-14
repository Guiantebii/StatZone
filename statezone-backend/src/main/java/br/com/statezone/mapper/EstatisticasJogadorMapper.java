package br.com.statezone.mapper;

import br.com.statezone.dto.rankings.*;
import br.com.statezone.dto.estatisticasJogador.EstatisticasJogadorResponseDto;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EstatisticasJogadorMapper {


    @Mapping(source = "jogador.id", target = "jogadorId")
    @Mapping(source = "jogador.nome", target = "nomeJogador")
    @Mapping(source = "jogador.time.nome", target = "nomeTime")
    @Mapping(source = "cleanSheets", target = "cleanSheets")
    @Mapping(target = "mediaGolsPorPartida", expression = "java(calcularMedia(entity.getGols(), entity.getPartidasJogadas()))")
    @Mapping(target = "mediaAssistenciasPorPartida", expression = "java(calcularMedia(entity.getAssistencias(), entity.getPartidasJogadas()))")
    @Mapping(target = "mediaDefesasPorPartida", expression = "java(calcularMedia(entity.getDefesas(), entity.getPartidasJogadas()))")
    EstatisticasJogadorResponseDto toDto(EstatisticasJogador entity);

    default Double calcularMedia(Integer valor, Integer partidas) {
        if (partidas == null || partidas == 0) return 0.0;
        return Math.round((valor / (double) partidas) * 100.0) / 100.0;
    }

    @Mapping(source = "entity.jogador.id", target = "jogadorId")
    @Mapping(source = "entity.jogador.nome", target = "nomeJogador")
    @Mapping(source = "entity.jogador.time.nome", target = "nomeTime")
    @Mapping(source = "entity.jogador.time.escudoUrl", target = "escudoTime")
    @Mapping(source = "entity.gols", target = "gols")
    @Mapping(source = "entity.assistencias", target = "assistencias")
    @Mapping(source = "entity.defesas", target = "defesas")
    @Mapping(source = "entity.penaltisDefendidos", target = "penaltisDefendidos")
    @Mapping(source = "entity.penaltisPerdidos", target = "penaltisPerdidos")
    @Mapping(source = "entity.cartoesAmarelos", target = "cartoesAmarelos")
    @Mapping(source = "entity.cartoesVermelhos", target = "cartoesVermelhos")
    @Mapping(source = "score", target = "score")
    CraqueCampeonatoResponseDto toCraqueCampeonatoDto(
            EstatisticasJogadorCampeonato entity,
            Double score
    );


}