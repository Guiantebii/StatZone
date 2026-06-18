package br.com.statezone.service;

import br.com.statezone.dto.partida.PartidaRequestDto;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.events.EventoPartidaCriadaEvent;
import br.com.statezone.events.PartidaEncerradaEvent;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.model.*;
import br.com.statezone.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher publisher;
    private final PartidaWebSocketService partidaWebSocketService;
    private final ConfrontoEliminatorioRepository confrontoEliminatorioRepository;
    private final EventoPartidaRepository eventoPartidaRepository;

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

    public List<PartidaResponseDto> listarTodas() {
        return partidaRepository.findAll()
                .stream()
                .map(partidaMapper::toDto)
                .toList();
    }

    public PartidaResponseDto buscarPorId(Long id) {
        return partidaMapper.toDto(buscarPartida(id));
    }

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

    public PartidaResponseDto iniciar(Long id) {

        Partida partida = buscarPartida(id);

        validarStatusNaoIniciadaOuEncerrada(partida);

        partida.setStatus(StatusPartida.AO_VIVO);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.INICIO_PRIMEIRO_TEMPO, 1);

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto intervalo(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException("Partida não está ao vivo");
        }

        partida.setStatus(StatusPartida.INTERVALO);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.FIM_PRIMEIRO_TEMPO, 45);

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto iniciarSegundoTempo(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.INTERVALO) {
            throw new BusinessException("Partida não está no intervalo");
        }

        partida.setStatus(StatusPartida.AO_VIVO);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.INICIO_SEGUNDO_TEMPO, 46);

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto encerrar(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException("Só partidas ao vivo podem ser encerradas");
        }

        partida.setStatus(StatusPartida.ENCERRADA);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.FIM_PARTIDA, 90);

        publisher.publishEvent(new PartidaEncerradaEvent(salva));

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }


    public PartidaResponseDto woMandante(Long id) {

        Partida partida = buscarPartida(id);

        validarNaoEncerradaOuWO(partida);

        partida.setStatus(StatusPartida.WO_MANDANTE);
        partida.setGolsMandante(0);
        partida.setGolsVisitante(3);

        Partida salva = partidaRepository.save(partida);
        criarEventoSistema(salva, TipoEvento.FIM_PARTIDA, 90);
        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));
        return partidaMapper.toDto(salva);
    }

    public PartidaResponseDto woVisitante(Long id) {

        Partida partida = buscarPartida(id);

        validarNaoEncerradaOuWO(partida);

        partida.setStatus(StatusPartida.WO_VISITANTE);
        partida.setGolsMandante(3);
        partida.setGolsVisitante(0);

        Partida salva = partidaRepository.save(partida);
        criarEventoSistema(salva, TipoEvento.FIM_PARTIDA, 90);
        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));
        return partidaMapper.toDto(salva);
    }

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

    public PartidaResponseDto adiar(Long id) {
        return alterarStatusAdministrativo(id, StatusPartida.ADIADA);
    }

    public PartidaResponseDto cancelar(Long id) {
        return alterarStatusAdministrativo(id, StatusPartida.CANCELADA);
    }


    public PartidaResponseDto iniciarPenaltis(Long id) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException("Partida não está ao vivo");
        }

        partida.setStatus(StatusPartida.PENALTIS);
        partida.setGolsPenaltisMandante(0);
        partida.setGolsPenaltisVisitante(0);

        return partidaMapper.toDto(partidaRepository.save(partida));
    }

    public PartidaResponseDto encerrarComPenaltis(
            Long id,
            Integer penA,
            Integer penB
    ) {

        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.PENALTIS) {
            throw new BusinessException("Partida não está em pênaltis");
        }

        if (penA.equals(penB)) {
            throw new BusinessException("Pênaltis não podem empatar");
        }

        partida.setGolsPenaltisMandante(penA);
        partida.setGolsPenaltisVisitante(penB);
        partida.setStatus(StatusPartida.ENCERRADA);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.FIM_PARTIDA, 120);
        publisher.publishEvent(new PartidaEncerradaEvent(salva));

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
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

    private void validarStatusNaoIniciadaOuEncerrada(Partida p) {
        if (p.getStatus() == StatusPartida.AO_VIVO) {
            throw new ConflictException("Já está em andamento");
        }
        if (p.getStatus() == StatusPartida.ENCERRADA) {
            throw new BusinessException("Já encerrada");
        }
    }

    private void validarNaoEncerradaOuWO(Partida p) {
        if (p.getStatus() == StatusPartida.ENCERRADA) {
            throw new BusinessException("Já encerrada");
        }

        if (p.getStatus() == StatusPartida.WO_MANDANTE ||
                p.getStatus() == StatusPartida.WO_VISITANTE) {
            throw new ConflictException("Já existe WO");
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
        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));
        return partidaMapper.toDto(salva);
    }

    private void criarEventoSistema(
            Partida partida,
            TipoEvento tipo,
            Integer minuto
    ) {
        EventoPartida evento = new EventoPartida();
        evento.setPartida(partida);
        evento.setTipoEvento(tipo);
        evento.setMinuto(minuto);
        evento.setDescricao(tipo.name());
        eventoPartidaRepository.save(evento);
        publisher.publishEvent(new EventoPartidaCriadaEvent(evento, partida));
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
