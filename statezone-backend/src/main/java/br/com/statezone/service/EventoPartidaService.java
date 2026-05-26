package br.com.statezone.service;

import br.com.statezone.dto.EventoPartidaRequestDto;
import br.com.statezone.dto.EventoPartidaResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
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

        public EventoPartidaResponseDto registrarEvento(EventoPartidaRequestDto dto,Long partidaId){
                Partida partida = partidaRepository.findById(partidaId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Partida não encontrada"));
                Jogador jogador = jogadorRepository.findById(dto.jogadorId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Jogador não encontrado"));

                validarJogadorNaPartida(partida,jogador);
                validarStatusPartida(partida);
                EventoPartida evento =
                        eventoPartidaMapper.toEntity(dto);

                evento.setPartida(partida);

                evento.setJogador(jogador);

                EventoPartida eventoSalvo =
                        eventoPartidaRepository.save(evento);
                if(dto.tipoEvento() == TipoEvento.GOL){

                        atualizarPlacar(partida, jogador);

                        partidaRepository.save(partida);
                }

                return eventoPartidaMapper.toDto(eventoSalvo);
        }


        public List<EventoPartidaResponseDto> listarEventosPorPartida(Long partidaId){
                Partida partida = partidaRepository.findById(partidaId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Partida não encontrada"));

                return eventoPartidaRepository
                        .findByPartidaIdOrderByMinutoAscMinutoExtraAsc(partidaId)
                        .stream()
                        .map(eventoPartidaMapper::toDto)
                        .toList();
        }

        private void validarJogadorNaPartida(Partida partida, Jogador jogador) {

                boolean pertenceMandante =
                        jogador.getTime().getId()
                                .equals(partida.getTimeMandante().getId());

                boolean pertenceVisitante =
                        jogador.getTime().getId()
                                .equals(partida.getTimeVisitante().getId());

                if (!pertenceMandante && !pertenceVisitante) {

                        throw new IllegalArgumentException(
                                "Jogador não pertence aos times da partida");
                }
        }
        private void atualizarPlacar(
                Partida partida,
                Jogador jogador
        ) {

                boolean mandante =
                        jogador.getTime().getId()
                                .equals(partida.getTimeMandante().getId());

                if(mandante){

                        partida.setGolsMandante(
                                partida.getGolsMandante() + 1
                        );

                } else {

                        partida.setGolsVisitante(
                                partida.getGolsVisitante() + 1
                        );
                }
        }

        private void validarStatusPartida(Partida partida) {

                if(partida.getStatus() != StatusPartida.AO_VIVO){

                        throw new IllegalArgumentException(
                                "Só é possível registrar eventos em partidas ao vivo"
                        );
                }
        }
}
