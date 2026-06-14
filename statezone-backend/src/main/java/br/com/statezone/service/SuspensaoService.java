package br.com.statezone.service;

import br.com.statezone.dto.suspensao.SuspensaoResponseDto;
import br.com.statezone.mapper.SuspensaoMapper;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.SuspensaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuspensaoService {

    private final SuspensaoRepository suspensaoRepository;
    private final PartidaRepository partidaRepository;
    private final SuspensaoMapper suspensaoMapper;

    public List<SuspensaoResponseDto> listarSuspensoesPorRodada(
            Long campeonatoId,
            Integer rodada
    ) {
        return suspensaoRepository
                .findByCampeonatoIdAndRodadaSuspensao(campeonatoId, rodada)
                .stream()
                .map(suspensaoMapper::toDto)
                .toList();
    }

    public List<SuspensaoResponseDto> listarSuspensoesProximaRodada(Long campeonatoId) {
        Integer proximaRodada = partidaRepository
                .findProximaRodada(campeonatoId);

        if (proximaRodada == null) {
            return List.of();
        }

        return listarSuspensoesPorRodada(campeonatoId, proximaRodada);
    }
}