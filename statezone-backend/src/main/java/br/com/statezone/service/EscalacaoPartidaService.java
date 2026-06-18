package br.com.statezone.service;

import br.com.statezone.dto.escalacao.EscalacaoPartidaListResponseDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaRequestDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaResponseDto;
import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.EscalacaoPartidaMapper;
import br.com.statezone.model.EscalacaoPartida;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.repository.EscalacaoPartidaRepository;
import br.com.statezone.repository.JogadorRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.SuspensaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EscalacaoPartidaService {

    private final EscalacaoPartidaRepository escalacaoPartidaRepository;
    private final PartidaRepository partidaRepository;
    private final JogadorRepository jogadorRepository;
    private final EscalacaoPartidaMapper escalacaoPartidaMapper;
    private final SuspensaoRepository suspensaoRepository;

    public EscalacaoPartidaResponseDto adicionarJogador(
            Long partidaId,
            EscalacaoPartidaRequestDto dto
    ) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Partida não encontrada"));

        if (partida.getStatus() == StatusPartida.ENCERRADA) {
            throw new BusinessException(
                    "Não é possível alterar escalação de partida encerrada");
        }

        Jogador jogador = jogadorRepository.findById(dto.jogadorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Jogador não encontrado"));

        validarJogadorNaPartida(partida, jogador);

        boolean suspenso = suspensaoRepository.existsByJogadorIdAndCampeonatoIdAndPartidaAlvoId(
                jogador.getId(), partida.getCampeonato().getId(), partida.getId());

        if (suspenso) {
            throw new BusinessException("Jogador está suspenso para esta partida");
        }

        if (escalacaoPartidaRepository.existsByPartidaIdAndJogadorId(partidaId, dto.jogadorId())) {
            throw new ConflictException("Jogador já está na escalação desta partida");
        }

        EscalacaoPartida escalacao = new EscalacaoPartida();
        escalacao.setPartida(partida);
        escalacao.setJogador(jogador);
        escalacao.setFuncao(dto.funcao());
        escalacao.setPosicao(dto.posicao() != null ? dto.posicao() : jogador.getPosicao());
        escalacao.setNumeroCamisa(dto.numeroCamisa() != null ? dto.numeroCamisa() : jogador.getNumeroCamisa());
        escalacao.setAtivo(true);

        return escalacaoPartidaMapper.toDto(escalacaoPartidaRepository.save(escalacao));
    }

    public EscalacaoPartidaListResponseDto buscarEscalacao(Long partidaId) {

        partidaRepository.findById(partidaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Partida não encontrada"));

        List<EscalacaoPartidaResponseDto> todos = escalacaoPartidaRepository
                .findByPartidaIdWithJogador(partidaId)
                .stream()
                .map(escalacaoPartidaMapper::toDto)
                .toList();

        List<EscalacaoPartidaResponseDto> titulares = todos.stream()
                .filter(e -> e.funcao() == FuncaoEscalacao.TITULAR)
                .toList();

        List<EscalacaoPartidaResponseDto> reservas = todos.stream()
                .filter(e -> e.funcao() == FuncaoEscalacao.RESERVA)
                .toList();

        return new EscalacaoPartidaListResponseDto(partidaId, titulares, reservas);
    }

    private void validarJogadorNaPartida(Partida partida, Jogador jogador) {
        boolean ok =
                jogador.getTime().getId().equals(partida.getTimeMandante().getId()) ||
                        jogador.getTime().getId().equals(partida.getTimeVisitante().getId());

        if (!ok) {
            throw new BusinessException("Jogador não pertence aos times da partida");
        }
    }
}