package br.com.statezone.service;

import br.com.statezone.dto.partida.FormacaoUpdateRequest;
import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.enums.StatusCampeonato;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.model.*;
import br.com.statezone.repository.*;
import br.com.statezone.service.helper.CampeonatoAccessHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.statezone.enums.StatusPartida.*;

@Service
@RequiredArgsConstructor
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final TimeRepository timeRepository;
    private final PartidaMapper partidaMapper;
    private final ConfrontoEliminatorioRepository confrontoEliminatorioRepository;
    private final CampeonatoAccessHelper campeonatoAccessHelper;

    @Transactional
    public PartidaResponseDto criar(PartidaRequestDto dto) {

        Campeonato campeonato = buscarCampeonato(dto.campeonatoId());
        Time mandante = buscarTime(dto.timeMandanteId(), "mandante");
        Time visitante = buscarTime(dto.timeVisitanteId(), "visitante");

        validarTimes(mandante, visitante);

        Partida entity = partidaMapper.toEntity(dto);
        entity.setCampeonato(campeonato);
        entity.setTimeMandante(mandante);
        entity.setTimeVisitante(visitante);

        definirGolsIniciais(entity);

        return partidaMapper.toDto(partidaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<PartidaResponseDto> listarTodas(Pageable pageable) {
        return partidaRepository.findAllWithTimes(pageable)
                .stream()
                .filter(this::partidaVisivelParaUsuarioAtual)
                .map(partidaMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PartidaResponseDto buscarPorId(Long id) {
        Partida partida = buscarPartida(id);
        campeonatoAccessHelper.validarVisibilidade(partida);
        return partidaMapper.toDto(partida);
    }

    @Transactional
    public PartidaResponseDto atualizar(Long id, PartidaRequestDto dto) {

        Partida partida = buscarPartida(id);

        Campeonato campeonato = buscarCampeonato(dto.campeonatoId());
        Time mandante = buscarTime(dto.timeMandanteId(), "mandante");
        Time visitante = buscarTime(dto.timeVisitanteId(), "visitante");

        validarTimes(mandante, visitante);

        partidaMapper.updatePartidaFromDto(dto, partida);

        partida.setCampeonato(campeonato);
        partida.setTimeMandante(mandante);
        partida.setTimeVisitante(visitante);

        return partidaMapper.toDto(partidaRepository.save(partida));
    }

    @Transactional
    public void deletar(Long id) {
        Partida partida = buscarPartida(id);

        if (confrontoEliminatorioRepository.findConfrontoByPartidaId(id).isPresent()) {
            throw new BusinessException("Não é possível excluir uma partida vinculada a um confronto eliminatório");
        }

        if (partida.getStatus() == StatusPartida.AO_VIVO
                || partida.getStatus() == StatusPartida.INTERVALO
                || partida.getStatus() == StatusPartida.PENALTIS
                || partida.getStatus() == StatusPartida.ENCERRADA
                || partida.getStatus() == StatusPartida.WO_MANDANTE
                || partida.getStatus() == StatusPartida.WO_VISITANTE) {
            throw new BusinessException("Não é possível excluir uma partida já em andamento ou finalizada");
        }

        partidaRepository.delete(partida);
    }

    @Transactional
    public PartidaResponseDto atualizarFormacao(Long id, FormacaoUpdateRequest dto) {
        Partida partida = buscarPartida(id);

        if (partida.getTimeMandante().getId().equals(dto.timeId())) {
            partida.setFormacaoMandante(dto.formacao());
        } else if (partida.getTimeVisitante().getId().equals(dto.timeId())) {
            partida.setFormacaoVisitante(dto.formacao());
        } else {
            throw new br.com.statezone.exception.ResourceNotFoundException("Time não pertence a esta partida");
        }

        return partidaMapper.toDto(partidaRepository.save(partida));
    }

    @Transactional
    public PartidaResponseDto adiar(Long id) {
        return alterarStatusAdministrativo(id, ADIADA);
    }

    @Transactional
    public PartidaResponseDto cancelar(Long id) {
        return alterarStatusAdministrativo(id, CANCELADA);
    }

    private Partida buscarPartida(Long id) {
        return partidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partida não encontrada"));
    }

    private Campeonato buscarCampeonato(Long id) {
        return campeonatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));
    }

    private Time buscarTime(Long id, String tipo) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time " + tipo + " não encontrado"));
    }

    private void validarTimes(Time a, Time b) {
        if (a.getId().equals(b.getId())) {
            throw new BusinessException("Times não podem ser iguais");
        }
    }

    private PartidaResponseDto alterarStatusAdministrativo(Long id, StatusPartida novoStatus) {
        Partida partida = buscarPartida(id);

        if (partida.getStatus() == StatusPartida.AO_VIVO
                || partida.getStatus() == StatusPartida.INTERVALO
                || partida.getStatus() == StatusPartida.PENALTIS
                || partida.getStatus() == StatusPartida.ENCERRADA
                || partida.getStatus() == StatusPartida.WO_MANDANTE
                || partida.getStatus() == StatusPartida.WO_VISITANTE) {
            throw new BusinessException("Não é possível alterar o status de uma partida em andamento ou finalizada");
        }

        if (partida.getStatus() == novoStatus) {
            throw new ConflictException("A partida já está com o status " + novoStatus);
        }

        partida.setStatus(novoStatus);
        Partida salva = partidaRepository.save(partida);
        return partidaMapper.toDto(salva);
    }

    private void definirGolsIniciais(Partida partida) {
        if (partida.getGolsMandante() == null) {
            partida.setGolsMandante(0);
        }

        if (partida.getGolsVisitante() == null) {
            partida.setGolsVisitante(0);
        }
    }

    private boolean partidaVisivelParaUsuarioAtual(Partida partida) {
        if (campeonatoAccessHelper.podeVerRascunho()) {
            return true;
        }

        return partida.getCampeonato() != null
                && partida.getCampeonato().getStatus() == StatusCampeonato.ATIVO;
    }
}
