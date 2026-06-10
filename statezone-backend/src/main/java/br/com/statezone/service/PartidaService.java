package br.com.statezone.service;

import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.EventoPartidaMapper;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.model.*;
import br.com.statezone.repository.*;
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
    private final JogadorRepository jogadorRepository;
    private final EstatisticasJogadorRepository estatisticasJogadorRepository;
    private final EventoPartidaRepository eventoPartidaRepository;
    private final EventoPartidaMapper eventoPartidaMapper;
    private final EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;

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

        criarEventoSistema(
                salva,
                TipoEvento.INICIO_PRIMEIRO_TEMPO,
                1
        );

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto encerrar(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException(
                    "Só partidas ao vivo podem ser encerradas"
            );
        }

        partida.setStatus(StatusPartida.ENCERRADA);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(
                salva,
                TipoEvento.FIM_PARTIDA,
                90
        );

        atualizarPartidasJogadasDosAtletas(salva);

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

    private void atualizarPartidasJogadasDosAtletas(Partida partida) {
        List<Jogador> jogadores = jogadorRepository.findByTimeIdIn(
                List.of(partida.getTimeMandante().getId(),
                        partida.getTimeVisitante().getId())
        );

        for (Jogador jogador : jogadores) {

            // carreira
            EstatisticasJogador carreira = estatisticasJogadorRepository
                    .findByJogadorId(jogador.getId())
                    .orElseGet(() -> {
                        EstatisticasJogador s = new EstatisticasJogador();
                        s.setJogador(jogador);
                        return s;
                    });
            carreira.setPartidasJogadas(carreira.getPartidasJogadas() + 1);
            estatisticasJogadorRepository.save(carreira);

            // campeonato
            EstatisticasJogadorCampeonato campeonato = estatisticasJogadorCampeonatoRepository
                    .findByJogadorIdAndCampeonatoId(
                            jogador.getId(),
                            partida.getCampeonato().getId()
                    )
                    .orElseGet(() -> {
                        EstatisticasJogadorCampeonato s = new EstatisticasJogadorCampeonato();
                        s.setJogador(jogador);
                        s.setCampeonato(partida.getCampeonato());
                        return s;
                    });
            campeonato.setPartidasJogadas(campeonato.getPartidasJogadas() + 1);
            estatisticasJogadorCampeonatoRepository.save(campeonato);
        }
    }
    private void criarEventoSistema(
            Partida partida,
            TipoEvento tipoEvento,
            Integer minuto
    ) {

        EventoPartida evento = new EventoPartida();

        evento.setPartida(partida);
        evento.setTipoEvento(tipoEvento);
        evento.setMinuto(minuto);

        eventoPartidaRepository.save(evento);
    }

    public PartidaResponseDto intervalo(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException(
                    "Só é possível pausar para intervalo partidas ao vivo");
        }

        partida.setStatus(StatusPartida.INTERVALO);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(
                salva,
                TipoEvento.FIM_PRIMEIRO_TEMPO,
                45
        );

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto iniciarSegundoTempo(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.INTERVALO) {
            throw new BusinessException(
                    "Só é possível iniciar o segundo tempo após o intervalo");
        }

        partida.setStatus(StatusPartida.AO_VIVO);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(
                salva,
                TipoEvento.INICIO_SEGUNDO_TEMPO,
                46
        );

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto adiar(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() == StatusPartida.ENCERRADA) {
            throw new BusinessException("Partida já foi encerrada");
        }

        if (partida.getStatus() == StatusPartida.ADIADA) {
            throw new ConflictException("Partida já está adiada");
        }

        partida.setStatus(StatusPartida.ADIADA);

        Partida salva = partidaRepository.save(partida);

        rankingCacheService.recalcular(salva.getCampeonato().getId());

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto cancelar(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() == StatusPartida.ENCERRADA) {
            throw new BusinessException("Partida já foi encerrada");
        }

        if (partida.getStatus() == StatusPartida.CANCELADA) {
            throw new ConflictException("Partida já está cancelada");
        }

        partida.setStatus(StatusPartida.CANCELADA);

        Partida salva = partidaRepository.save(partida);

        rankingCacheService.recalcular(salva.getCampeonato().getId());

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto woMandante(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() == StatusPartida.ENCERRADA) {
            throw new BusinessException("Partida já foi encerrada");
        }

        if (partida.getStatus() == StatusPartida.WO_MANDANTE ||
                partida.getStatus() == StatusPartida.WO_VISITANTE) {
            throw new ConflictException("Partida já tem WO registrado");
        }

        partida.setStatus(StatusPartida.WO_MANDANTE);
        partida.setGolsMandante(0);
        partida.setGolsVisitante(3);

        Partida salva = partidaRepository.save(partida);

        rankingCacheService.recalcular(salva.getCampeonato().getId());

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto woVisitante(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() == StatusPartida.ENCERRADA) {
            throw new BusinessException("Partida já foi encerrada");
        }

        if (partida.getStatus() == StatusPartida.WO_MANDANTE ||
                partida.getStatus() == StatusPartida.WO_VISITANTE) {
            throw new ConflictException("Partida já tem WO registrado");
        }

        partida.setStatus(StatusPartida.WO_VISITANTE);
        partida.setGolsMandante(3);
        partida.setGolsVisitante(0);

        Partida salva = partidaRepository.save(partida);

        rankingCacheService.recalcular(salva.getCampeonato().getId());

        return partidaMapper.toDto(salva);
    }
}