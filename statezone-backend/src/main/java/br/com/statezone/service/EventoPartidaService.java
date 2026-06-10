package br.com.statezone.service;

import br.com.statezone.dto.eventoPartida.EventoPartidaRequestDto;
import br.com.statezone.dto.eventoPartida.EventoPartidaResponseDto;
import br.com.statezone.dto.eventoPartida.EventoTimelineResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.events.EventoPartidaCriadaEvent;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.EventoPartidaMapper;
import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.repository.EventoPartidaRepository;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.PartidaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventoPartidaService {

        private final EventoPartidaRepository eventoPartidaRepository;
        private final PartidaRepository partidaRepository;
        private final JogadorRepository jogadorRepository;
        private final EventoPartidaMapper eventoPartidaMapper;
        private final ApplicationEventPublisher publisher;

        public EventoPartidaResponseDto registrarEvento(
                EventoPartidaRequestDto dto,
                Long partidaId
        ) {

                Partida partida = partidaRepository.findById(partidaId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Partida não encontrada"));

                validarStatusPartida(partida);

                if (exigeJogador(dto.tipoEvento()) && dto.jogadorId() == null) {
                        throw new BusinessException(
                                "Este tipo de evento exige um jogador");
                }

                Jogador jogador = null;

                if (dto.jogadorId() != null) {

                        jogador = jogadorRepository.findById(dto.jogadorId())
                                .orElseThrow(() ->
                                        new ResourceNotFoundException("Jogador não encontrado"));

                        validarJogadorNaPartida(partida, jogador);
                }

                Jogador jogadorSecundario = null;

                if (dto.jogadorSecundarioId() != null) {

                        if (dto.tipoEvento() != TipoEvento.GOL &&
                                dto.tipoEvento() != TipoEvento.PENALTI_GOL) {

                                throw new BusinessException(
                                        "Este tipo de evento não aceita jogador secundário");
                        }

                        jogadorSecundario = jogadorRepository.findById(dto.jogadorSecundarioId())
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Jogador secundário não encontrado"));

                        validarJogadorNaPartida(partida, jogadorSecundario);

                        if (jogador != null &&
                                jogadorSecundario.getId().equals(jogador.getId())) {

                                throw new BusinessException(
                                        "O jogador principal não pode ser o jogador secundário");
                        }
                }

                EventoPartida evento = eventoPartidaMapper.toEntity(dto);

                evento.setPartida(partida);
                evento.setJogador(jogador);
                evento.setJogadorSecundario(jogadorSecundario);

                if (jogador != null) {
                        evento.setTime(jogador.getTime());
                }

                EventoPartida salvo = eventoPartidaRepository.save(evento);

                aplicarGolSeNecessario(partida, evento);

                partidaRepository.save(partida);

                publisher.publishEvent(
                        new EventoPartidaCriadaEvent(salvo, partida)
                );

                return eventoPartidaMapper.toDto(salvo);
        }

        public List<EventoPartidaResponseDto> listarEventosPorPartida(Long partidaId) {

                partidaRepository.findById(partidaId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Partida não encontrada"));

                return eventoPartidaRepository
                        .findByPartidaIdOrderByMinutoAscMinutoExtraAscCriadoEmAsc(partidaId)
                        .stream()
                        .map(eventoPartidaMapper::toDto)
                        .toList();
        }

        public List<EventoTimelineResponseDto> buscarTimeline(Long partidaId) {

                partidaRepository.findById(partidaId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Partida não encontrada"));

                return eventoPartidaRepository
                        .findByPartidaIdOrderByMinutoAscMinutoExtraAscCriadoEmAsc(partidaId)
                        .stream()
                        .map(eventoPartidaMapper::toTimelineDto)
                        .toList();
        }

        private void aplicarGolSeNecessario(Partida partida, EventoPartida evento) {

                if (evento.getTime() == null) {
                        return;
                }

                TipoEvento tipo = evento.getTipoEvento();

                boolean mandante =
                        evento.getTime().getId().equals(partida.getTimeMandante().getId());

                switch (tipo) {

                        case GOL, PENALTI_GOL -> {
                                if (mandante) {
                                        partida.setGolsMandante(partida.getGolsMandante() + 1);
                                } else {
                                        partida.setGolsVisitante(partida.getGolsVisitante() + 1);
                                }
                        }

                        case GOL_CONTRA -> {
                                if (mandante) {
                                        partida.setGolsVisitante(partida.getGolsVisitante() + 1);
                                } else {
                                        partida.setGolsMandante(partida.getGolsMandante() + 1);
                                }
                        }

                        default -> {
                        }
                }
        }

        private void validarJogadorNaPartida(Partida partida, Jogador jogador) {

                boolean ok =
                        jogador.getTime().getId().equals(partida.getTimeMandante().getId()) ||
                                jogador.getTime().getId().equals(partida.getTimeVisitante().getId());

                if (!ok) {
                        throw new BusinessException(
                                "Jogador não pertence aos times da partida");
                }
        }

        private void validarStatusPartida(Partida partida) {
                if (partida.getStatus() != StatusPartida.AO_VIVO) {
                        throw new BusinessException(
                                "Só é possível registrar eventos em partidas ao vivo. " +
                                        "Status atual: " + partida.getStatus());
                }
        }

        private boolean exigeJogador(TipoEvento tipo) {

                return switch (tipo) {

                        case GOL,
                             GOL_CONTRA,
                             PENALTI_GOL,
                             PENALTI_PERDIDO,

                             FINALIZACAO,
                             FINALIZACAO_NO_GOL,

                             DEFESA,
                             PENALTI_DEFENDIDO,

                             FALTA,
                             CARTAO_AMARELO,
                             CARTAO_VERMELHO,

                             IMPEDIMENTO,
                             ESCANTEIO,

                             SUBSTITUICAO -> true;

                        case VAR_GOL_CONFIRMADO,
                             VAR_GOL_ANULADO,
                             INICIO_PRIMEIRO_TEMPO,
                             FIM_PRIMEIRO_TEMPO,
                             INICIO_SEGUNDO_TEMPO,
                             FIM_PARTIDA -> false;
                };
        }
}