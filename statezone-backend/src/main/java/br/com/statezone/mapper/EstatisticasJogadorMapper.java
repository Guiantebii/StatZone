package br.com.statezone.mapper;

import br.com.statezone.dto.rankings.*;
import br.com.statezone.dto.estatisticasJogador.EstatisticasJogadorResponseDto;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EstatisticasJogadorMapper {

    // ─── Carreira ───────────────────────────────────────────────
    @Mapping(source = "jogador.id", target = "jogadorId")
    @Mapping(source = "jogador.nome", target = "nomeJogador")
    @Mapping(source = "jogador.time.nome", target = "nomeTime")
    EstatisticasJogadorResponseDto toDto(EstatisticasJogador entity);

    // ─── Campeonato ──────────────────────────────────────────────
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