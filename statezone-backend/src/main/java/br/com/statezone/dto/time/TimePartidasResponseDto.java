package br.com.statezone.dto.time;

import br.com.statezone.dto.partida.PartidaResponseDto;

import java.util.List;

public record TimePartidasResponseDto(
        Long timeId,
        List<PartidaResponseDto> ultimasPartidas,
        List<PartidaResponseDto> proximasPartidas
) {}
