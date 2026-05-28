package br.com.statezone.service;

import br.com.statezone.dto.PartidaRequestDto;
import br.com.statezone.dto.PartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.TimeRepository;
import br.com.statezone.service.ranking.RankingCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final TimeRepository timeRepository;
    private final PartidaMapper partidaMapper;
    private final RankingCacheService rankingCacheService;

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

        Partida salvo = partidaRepository.save(entity);

        return partidaMapper.toDto(salvo);
    }

    public List<PartidaResponseDto> listarTodas() {

        return partidaRepository.findAll()
                .stream()
                .map(partidaMapper::toDto)
                .toList();
    }

    public PartidaResponseDto buscarPorId(Long id) {

        Partida partida = buscarPartida(id);

        return partidaMapper.toDto(partida);
    }

    public PartidaResponseDto atualizar(
            PartidaRequestDto dto,
            Long id
    ) {

        Partida partida = buscarPartida(id);

        Campeonato campeonato = buscarCampeonato(dto.campeonatoId());

        Time mandante = buscarTime(dto.timeMandanteId(), "mandante");

        Time visitante = buscarTime(dto.timeVisitanteId(), "visitante");

        validarTimes(mandante, visitante);

        partidaMapper.updatePartidaFromDto(dto, partida);

        partida.setCampeonato(campeonato);
        partida.setTimeMandante(mandante);
        partida.setTimeVisitante(visitante);

        Partida atualizado = partidaRepository.save(partida);

        return partidaMapper.toDto(atualizado);
    }

    public PartidaResponseDto iniciar(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() == StatusPartida.AO_VIVO) {
            throw new ConflictException("Partida já está em andamento");
        }

        if (partida.getStatus() == StatusPartida.ENCERRADA) {
            throw new BusinessException("Partida já foi encerrada");
        }

        partida.setStatus(StatusPartida.AO_VIVO);

        Partida salva = partidaRepository.save(partida);

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto encerrar(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException("Só partidas ao vivo podem ser encerradas");
        }

        partida.setStatus(StatusPartida.ENCERRADA);

        Partida salva = partidaRepository.save(partida);

        rankingCacheService.recalcular(
                salva.getCampeonato().getId()
        );


        return partidaMapper.toDto(salva);
    }



    public void deletar(Long id) {

        Partida partida = buscarPartida(id);

        partidaRepository.delete(partida);
    }

    private Partida buscarPartida(Long id) {

        return partidaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Partida não encontrada"));
    }

    private Campeonato buscarCampeonato(Long id) {

        return campeonatoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Campeonato não encontrado"));
    }

    private Time buscarTime(Long id, String tipo) {

        return timeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Time " + tipo + " não encontrado"));
    }

    private void validarTimes(
            Time mandante,
            Time visitante
    ) {

        if (mandante.getId().equals(visitante.getId())) {

            throw new BusinessException(
                    "Os times da partida não podem ser iguais");
        }
    }

    private void definirGolsIniciais(Partida partida) {

        if (partida.getGolsMandante() == null) {
            partida.setGolsMandante(0);
        }

        if (partida.getGolsVisitante() == null) {
            partida.setGolsVisitante(0);
        }
    }
}