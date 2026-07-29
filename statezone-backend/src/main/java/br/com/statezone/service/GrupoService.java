package br.com.statezone.service;


import br.com.statezone.dto.eliminatoria.GrupoRequestDto;
import br.com.statezone.dto.eliminatoria.GrupoResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoFormato;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.GrupoMapper;
import br.com.statezone.model.*;
import br.com.statezone.repository.*;
import br.com.statezone.service.helper.CampeonatoAccessHelper;
import br.com.statezone.service.helper.RoundRobinHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final TimeRepository timeRepository;
    private final GrupoMapper grupoMapper;
    private final PartidaRepository partidaRepository;
    private final CampeonatoAccessHelper campeonatoAccessHelper;
    private final RoundRobinHelper roundRobinHelper;


    public GrupoResponseDto criarGrupo(Long campeonatoId, GrupoRequestDto dto) {
        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));

        if (campeonato.getTipoFormato() == TipoFormato.PONTOS_CORRIDOS) {
            throw new BusinessException(
                    "Campeonatos de pontos corridos não suportam grupos");
        }

        if (grupoRepository.existsByCampeonatoIdAndNome(campeonatoId, dto.nome())) {
            throw new ConflictException("Grupo " + dto.nome() + " já existe neste campeonato");
        }

        Grupo grupo = new Grupo();
        grupo.setCampeonato(campeonato);
        grupo.setNome(dto.nome());

        return grupoMapper.toDto(grupoRepository.save(grupo));
    }

    public GrupoResponseDto adicionarTime(Long campeonatoId, Long grupoId, Long timeId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        if (!grupo.getCampeonato().getId().equals(campeonatoId)) {
            throw new BusinessException("Grupo não pertence a este campeonato");
        }

        Time time = timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Time não encontrado"));

        if (!grupo.getCampeonato().getTimes().contains(time)) {
            throw new BusinessException(
                    "Time não pertence a este campeonato");
        }

        if (grupo.getTimes().contains(time)) {
            throw new ConflictException("Time já está neste grupo");
        }

        if (grupoRepository.existsTimeEmAlgumGrupoDoCampeonato(
                campeonatoId,
                timeId)) {

            throw new ConflictException(
                    "Time já pertence a outro grupo deste campeonato");
        }
        grupo.getTimes().add(time);

        return grupoMapper.toDto(grupoRepository.save(grupo));
    }

    public List<GrupoResponseDto> listarGrupos(Long campeonatoId) {
        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));
        campeonatoAccessHelper.validarVisibilidade(campeonato);

        return grupoRepository.findByCampeonatoIdWithTimes(campeonatoId)
                .stream()
                .map(grupoMapper::toDto)
                .toList();
    }

    public GrupoResponseDto buscarPorId(Long campeonatoId, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        if (!grupo.getCampeonato().getId().equals(campeonatoId)) {
            throw new BusinessException("Grupo não pertence a este campeonato");
        }

        campeonatoAccessHelper.validarVisibilidade(grupo.getCampeonato());

        return grupoMapper.toDto(grupo);
    }

    public void gerarFixturesPorGrupo(Long campeonatoId, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        if (!grupo.getCampeonato().getId().equals(campeonatoId)) {
            throw new BusinessException("Grupo não pertence a este campeonato");
        }

        if (partidaRepository.existsByGrupoId(grupoId)) {
            throw new ConflictException("Fixtures já foram geradas para este grupo");
        }

        List<Time> times = new ArrayList<>(grupo.getTimes());

        if (times.size() < 2) {
            throw new BusinessException("Grupo precisa ter pelo menos 2 times");
        }

        Campeonato campeonato = grupo.getCampeonato();

        roundRobinHelper.gerarTurno(times, 1, (mandante, visitante) -> {
            Partida partida = new Partida();
            partida.setCampeonato(campeonato);
            partida.setTimeMandante(mandante);
            partida.setTimeVisitante(visitante);
            partida.setGolsMandante(0);
            partida.setGolsVisitante(0);
            partida.setStatus(StatusPartida.AGENDADA);
            partida.setGrupo(grupo);
            partidaRepository.save(partida);
        });
    }
    
}
