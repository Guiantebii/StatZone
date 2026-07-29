package br.com.statezone.service;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.events.EventoPartidaCriadaEvent;
import br.com.statezone.events.PartidaEncerradaEvent;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.mapper.PartidaMapper;
import br.com.statezone.dto.partida.PartidaResponseDto;
import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.Partida;
import br.com.statezone.model.ProcessamentoConfrontoPendente;
import br.com.statezone.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

import static br.com.statezone.enums.StatusPartida.*;

@Service
@RequiredArgsConstructor
public class PartidaLifecycleService {

    private static final int MINUTO_INICIO = 1;
    private static final int MINUTO_INTERVALO = 45;
    private static final int MINUTO_SEGUNDO_TEMPO = 46;
    private static final int MINUTO_FIM = 90;
    private static final int MINUTO_PRORROGACAO_FIM = 120;

    private final PartidaRepository partidaRepository;
    private final PartidaMapper partidaMapper;
    private final ApplicationEventPublisher publisher;
    private final PartidaWebSocketService partidaWebSocketService;
    private final EventoPartidaRepository eventoPartidaRepository;
    private final ProcessamentoConfrontoPendenteRepository processamentoConfrontoPendenteRepository;

    @Transactional
    public PartidaResponseDto iniciar(Long id) {
        Partida partida = partidaRepository.findByIdWithLock(id)
                .orElseThrow(() -> new br.com.statezone.exception.ResourceNotFoundException("Partida não encontrada"));
        if (!Set.of(AGENDADA, ADIADA).contains(partida.getStatus())) {
            throw new BusinessException("Partida não pode ser iniciada a partir do status " + partida.getStatus());
        }

        partida.setStatus(StatusPartida.AO_VIVO);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.INICIO_PRIMEIRO_TEMPO, MINUTO_INICIO);

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    @Transactional
    public PartidaResponseDto intervalo(Long id) {
        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException("Partida não está ao vivo");
        }

        partida.setStatus(StatusPartida.INTERVALO);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.FIM_PRIMEIRO_TEMPO, MINUTO_INTERVALO);

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    @Transactional
    public PartidaResponseDto iniciarSegundoTempo(Long id) {
        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.INTERVALO) {
            throw new BusinessException("Partida não está no intervalo");
        }

        partida.setStatus(StatusPartida.AO_VIVO);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.INICIO_SEGUNDO_TEMPO, MINUTO_SEGUNDO_TEMPO);

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    @Transactional
    public PartidaResponseDto encerrar(Long id) {
        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO && partida.getStatus() != StatusPartida.PENALTIS) {
            throw new BusinessException("Apenas partidas ao vivo ou em pênaltis podem ser encerradas");
        }

        partida.setStatus(StatusPartida.ENCERRADA);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.FIM_PARTIDA, MINUTO_FIM);

        processamentoConfrontoPendenteRepository.save(
                ProcessamentoConfrontoPendente.builder()
                        .partidaId(partida.getId())
                        .criadoEm(LocalDateTime.now())
                        .tentativas(0)
                        .resolvido(false)
                        .build()
        );
        publisher.publishEvent(new PartidaEncerradaEvent(salva));

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    @Transactional
    public PartidaResponseDto woMandante(Long id) {
        Partida partida = buscarPartida(id);

        validarNaoEncerradaOuWO(partida);

        partida.setStatus(StatusPartida.WO_MANDANTE);
        partida.setGolsMandante(0);
        partida.setGolsVisitante(3);

        Partida salva = partidaRepository.save(partida);
        criarEventoSistema(salva, TipoEvento.FIM_PARTIDA, MINUTO_FIM);
        processamentoConfrontoPendenteRepository.save(
                ProcessamentoConfrontoPendente.builder()
                        .partidaId(salva.getId())
                        .criadoEm(LocalDateTime.now())
                        .tentativas(0)
                        .resolvido(false)
                        .build()
        );
        publisher.publishEvent(new PartidaEncerradaEvent(salva));
        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));
        return partidaMapper.toDto(salva);
    }

    @Transactional
    public PartidaResponseDto woVisitante(Long id) {
        Partida partida = buscarPartida(id);

        validarNaoEncerradaOuWO(partida);

        partida.setStatus(StatusPartida.WO_VISITANTE);
        partida.setGolsMandante(3);
        partida.setGolsVisitante(0);

        Partida salva = partidaRepository.save(partida);
        criarEventoSistema(salva, TipoEvento.FIM_PARTIDA, MINUTO_FIM);
        processamentoConfrontoPendenteRepository.save(
                ProcessamentoConfrontoPendente.builder()
                        .partidaId(salva.getId())
                        .criadoEm(LocalDateTime.now())
                        .tentativas(0)
                        .resolvido(false)
                        .build()
        );
        publisher.publishEvent(new PartidaEncerradaEvent(salva));
        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));
        return partidaMapper.toDto(salva);
    }

    @Transactional
    public PartidaResponseDto iniciarProrrogacao(Long id) {
        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException("Partida não está ao vivo para iniciar prorrogação");
        }

        partida.setStatus(StatusPartida.PRORROGACAO);
        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.INICIO_PRORROGACAO, MINUTO_FIM);

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    @Transactional
    public PartidaResponseDto encerrarProrrogacao(Long id) {
        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.PRORROGACAO) {
            throw new BusinessException("Partida não está na prorrogação");
        }

        partida.setStatus(StatusPartida.PENALTIS);
        partida.setGolsPenaltisMandante(partida.getGolsPenaltisMandante() != null ? partida.getGolsPenaltisMandante() : 0);
        partida.setGolsPenaltisVisitante(partida.getGolsPenaltisVisitante() != null ? partida.getGolsPenaltisVisitante() : 0);

        Partida salva = partidaRepository.save(partida);

        criarEventoSistema(salva, TipoEvento.FIM_PRORROGACAO, MINUTO_PRORROGACAO_FIM);

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    @Transactional
    public PartidaResponseDto iniciarPenaltis(Long id) {
        Partida partida = buscarPartida(id);

        if (partida.getStatus() != StatusPartida.AO_VIVO) {
            throw new BusinessException("Partida não está ao vivo");
        }

        partida.setStatus(StatusPartida.PENALTIS);
        partida.setGolsPenaltisMandante(partida.getGolsPenaltisMandante() != null ? partida.getGolsPenaltisMandante() : 0);
        partida.setGolsPenaltisVisitante(partida.getGolsPenaltisVisitante() != null ? partida.getGolsPenaltisVisitante() : 0);

        return partidaMapper.toDto(partidaRepository.save(partida));
    }

    @Transactional
    public PartidaResponseDto encerrarComPenaltis(Long id, Integer penA, Integer penB) {
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

        criarEventoSistema(salva, TipoEvento.FIM_PARTIDA, MINUTO_PRORROGACAO_FIM);
        publisher.publishEvent(new PartidaEncerradaEvent(salva));

        partidaWebSocketService.notificarAtualizacaoPartida(partidaMapper.toDto(salva));

        return partidaMapper.toDto(salva);
    }

    private Partida buscarPartida(Long id) {
        return partidaRepository.findById(id)
                .orElseThrow(() -> new br.com.statezone.exception.ResourceNotFoundException("Partida não encontrada"));
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

    private void criarEventoSistema(Partida partida, TipoEvento tipo, Integer minuto) {
        EventoPartida evento = new EventoPartida();
        evento.setPartida(partida);
        evento.setTipoEvento(tipo);
        evento.setMinuto(minuto);
        evento.setDescricao(tipo.name());
        eventoPartidaRepository.save(evento);
        publisher.publishEvent(new EventoPartidaCriadaEvent(evento, partida));
    }
}