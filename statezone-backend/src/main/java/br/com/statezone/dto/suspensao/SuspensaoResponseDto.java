package br.com.statezone.dto.suspensao;

import br.com.statezone.enums.MotivoSuspensao;

public record SuspensaoResponseDto(
        Long jogadorId,
        String nomeJogador,
        String fotoUrl,
        String nomeTime,
        String escudoTime,
        Integer rodadaSuspensao,
        MotivoSuspensao motivo
) {}