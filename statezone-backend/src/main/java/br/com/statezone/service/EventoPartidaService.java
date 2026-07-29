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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventoPartidaService {

        private final EventoPartidaRepository eventoPartidaRepository;
        private final PartidaRepository partidaRepository;
        private final JogadorRepository jogadorRepository;
        private final EventoPartidaMapper eventoPartidaMapper;
        private final ApplicationEventPublisher publisher;

        @Transactional
        public EventoPartidaResponseDto registrarEvento(
                EventoPartidaRequestDto dto,
                Long partidaId
        ) {

                validarInputs(dto);

                Partida partida = partidaRepository.findByIdWithLock(partidaId)
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

                        if (!aceitaJogadorSecundario(dto.tipoEvento())) {

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
                } else if (exigeJogadorSecundario(dto.tipoEvento())) {
                        throw new BusinessException(
                                "Este tipo de evento exige um jogador secundário");
                }
                EventoPartida eventoRelacionado = null;
                if (dto.tipoEvento() == TipoEvento.VAR_GOL_ANULADO) {
                        if (dto.eventoRelacionadoId() == null) {
                                throw new BusinessException("VAR_GOL_ANULADO exige o evento de gol original (eventoRelacionadoId)");
                        }
                        eventoRelacionado = eventoPartidaRepository.findById(dto.eventoRelacionadoId())
                                .orElseThrow(() -> new ResourceNotFoundException("Evento original não encontrado"));

                        boolean tipoValido = eventoRelacionado.getTipoEvento() == TipoEvento.GOL
                                || eventoRelacionado.getTipoEvento() == TipoEvento.PENALTI_GOL
                                || eventoRelacionado.getTipoEvento() == TipoEvento.GOL_CONTRA;

                        if (!tipoValido || !eventoRelacionado.getPartida().getId().equals(partidaId)) {
                                throw new BusinessException("Evento relacionado inválido para anulação de VAR");
                        }
                        if (eventoRelacionado.isAnulado()) {
                                throw new BusinessException("Este gol já foi anulado anteriormente");
                        }
                        eventoRelacionado.setAnulado(true);
                        eventoPartidaRepository.save(eventoRelacionado);
                }



                EventoPartida evento = eventoPartidaMapper.toEntity(dto);

                evento.setPartida(partida);
                evento.setJogador(jogador);
                evento.setJogadorSecundario(jogadorSecundario);

                if (jogador != null) {
                        evento.setTime(jogador.getTime());
                } else if (
                        dto.tipoEvento() == TipoEvento.VAR_GOL_ANULADO
                                && eventoRelacionado != null
                                && eventoRelacionado.getTime() != null) {

                        evento.setTime(eventoRelacionado.getTime());
                }

                evento.setEventoRelacionado(eventoRelacionado);

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
                int golsMandante = (partida.getGolsMandante() != null) ? partida.getGolsMandante() : 0;
                int golsVisitante = (partida.getGolsVisitante() != null) ? partida.getGolsVisitante() : 0;

                if (evento.getTipoEvento() == TipoEvento.VAR_GOL_ANULADO) {
                        EventoPartida original = evento.getEventoRelacionado();
                        if (original == null || original.getTime() == null || partida.getTimeMandante() == null || partida.getTimeVisitante() == null) return;

                        boolean mandanteOriginal = Objects.equals(original.getTime().getId(), partida.getTimeMandante().getId());

                        if (original.getTipoEvento() == TipoEvento.GOL_CONTRA) {
                                if (mandanteOriginal) partida.setGolsVisitante(Math.max(0, golsVisitante - 1));
                                else partida.setGolsMandante(Math.max(0, golsMandante - 1));
                        } else {
                                if (mandanteOriginal) partida.setGolsMandante(Math.max(0, golsMandante - 1));
                                else partida.setGolsVisitante(Math.max(0, golsVisitante - 1));
                        }
                        return;
                }

                if (evento.getTime() == null || partida.getTimeMandante() == null || partida.getTimeVisitante() == null) return;
                boolean mandante = Objects.equals(evento.getTime().getId(), partida.getTimeMandante().getId());

                switch (evento.getTipoEvento()) {
                        case GOL, PENALTI_GOL -> {
                                if (mandante) partida.setGolsMandante(golsMandante + 1);
                                else partida.setGolsVisitante(golsVisitante + 1);
                        }
                        case GOL_CONTRA -> {
                                if (mandante) partida.setGolsVisitante(golsVisitante + 1);
                                else partida.setGolsMandante(golsMandante + 1);
                        }
                        default -> { }
                }
        }

        private void validarJogadorNaPartida(Partida partida, Jogador jogador) {
                if (jogador == null) {
                        throw new BusinessException("Jogador não informado");
                }

                if (jogador.getTime() == null) {
                        throw new BusinessException("Jogador não possui time vinculado");
                }

                if (partida.getTimeMandante() == null || partida.getTimeVisitante() == null) {
                        throw new BusinessException("Partida não tem times definidos");
                }

                boolean ok =
                        Objects.equals(jogador.getTime().getId(), partida.getTimeMandante().getId()) ||
                                Objects.equals(jogador.getTime().getId(), partida.getTimeVisitante().getId());

                if (!ok) {
                        throw new BusinessException(
                                "Jogador não pertence aos times da partida");
                }
        }

        private void validarStatusPartida(Partida partida) {
                if (partida.getStatus() == null) {
                        throw new BusinessException("Status da partida não está definido");
                }

                if (partida.getStatus() != StatusPartida.AO_VIVO
                        && partida.getStatus() != StatusPartida.PENALTIS) {
                        throw new BusinessException(
                                "Só é possível registrar eventos em partidas ao vivo ou em pênaltis. " +
                                        "Status atual: " + partida.getStatus());
                }
        }

        private void validarInputs(EventoPartidaRequestDto dto) {
                if (dto.tipoEvento() == null) {
                        throw new BusinessException("Tipo de evento é obrigatório");
                }

                if (dto.minuto() != null && (dto.minuto() < 0 || dto.minuto() > 150)) {
                        throw new BusinessException(
                                "Minuto deve estar entre 0 e 150. Valor fornecido: " + dto.minuto());
                }

                if (dto.minutoExtra() != null && (dto.minutoExtra() < 0 || dto.minutoExtra() > 50)) {
                        throw new BusinessException(
                                "Minuto extra deve estar entre 0 e 50. Valor fornecido: " + dto.minutoExtra());
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
                             INICIO_PRORROGACAO,
                             FIM_PRORROGACAO,
                             FIM_PARTIDA -> false;
                };
        }

        private boolean aceitaJogadorSecundario(TipoEvento tipo) {
                return switch (tipo) {
                        case GOL,
                             PENALTI_GOL,
                             SUBSTITUICAO,
                             PENALTI_DEFENDIDO -> true;
                        default -> false;
                };
        }

        private boolean exigeJogadorSecundario(TipoEvento tipo) {
                return switch (tipo) {
                        case SUBSTITUICAO,
                             PENALTI_DEFENDIDO -> true;
                        default -> false;
                };
        }
}
