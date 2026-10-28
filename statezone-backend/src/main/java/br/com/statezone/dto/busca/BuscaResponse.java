package br.com.statezone.dto.busca;

import br.com.statezone.dto.campeonato.CampeonatoResponseDto;
import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.dto.time.TimeResponseDto;

import java.util.List;

public record BuscaResponse(
        List<TimeResponseDto> times,
        List<JogadorResponseDto> jogadores,
        List<CampeonatoResponseDto> campeonatos
) {
}
