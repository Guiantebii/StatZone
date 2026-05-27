package br.com.statezone.service;

import br.com.statezone.dto.EventoPartidaRequestDto;
import br.com.statezone.dto.EventoPartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.events.EventoPartidaCriadaEvent;
import br.com.statezone.events.RankingAtualizadoEvent;
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

        public EventoPartidaResponseDto registrarEvento(EventoPartidaRequestDto dto, Long partidaId) {

                Partida partida = partidaRepository.findById(partidaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Partida não encontrada"));

                Jogador jogador = jogadorRepository.findById(dto.jogadorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado"));

                validarJogadorNaPartida(partida, jogador);
                validarStatusPartida(partida);

                EventoPartida evento = eventoPartidaMapper.toEntity(dto);

                evento.setPartida(partida);
                evento.setJogador(jogador);
                evento.setTime(jogador.getTime());

                EventoPartida salvo = eventoPartidaRepository.save(evento);

                aplicarGolSeNecessario(partida, evento);

                partidaRepository.save(partida);

                publisher.publishEvent(new EventoPartidaCriadaEvent(salvo, partida));
                publisher.publishEvent(
                        new RankingAtualizadoEvent(partida)
                );



                return eventoPartidaMapper.toDto(salvo);
        }

        public List<EventoPartidaResponseDto> listarEventosPorPartida(Long partidaId) {

                partidaRepository.findById(partidaId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Partida não encontrada"));

                return eventoPartidaRepository
                        .findByPartidaIdOrderByMinutoAscMinutoExtraAsc(partidaId)
                        .stream()
                        .map(eventoPartidaMapper::toDto)
                        .toList();
        }

        private void aplicarGolSeNecessario(Partida partida, EventoPartida evento) {

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

                        default -> {}
                }
        }

        private void validarJogadorNaPartida(Partida partida, Jogador jogador) {

                boolean ok =
                        jogador.getTime().getId().equals(partida.getTimeMandante().getId()) ||
                                jogador.getTime().getId().equals(partida.getTimeVisitante().getId());

                if (!ok) {
                        throw new BusinessException("Jogador não pertence aos times da partida");
                }
        }

        private void validarStatusPartida(Partida partida) {
                if (partida.getStatus() != StatusPartida.AO_VIVO) {
                        throw new BusinessException("Só é possível registrar eventos em partidas ao vivo");
                }
        }
}