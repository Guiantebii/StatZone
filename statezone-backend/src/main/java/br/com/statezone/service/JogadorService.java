package br.com.statezone.service;

import br.com.statezone.dto.jogador.JogadorRequestDto;
import br.com.statezone.dto.jogador.JogadorResponseDto;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.JogadorMapper;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Time;
import br.com.statezone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JogadorService {

    private final JogadorRepository jogadorRepository;
    private final JogadorMapper jogadorMapper;
    private final TimeRepository timeRepository;
    private final EventoPartidaRepository eventoPartidaRepository;
    private final EstatisticasJogadorRepository estatisticasJogadorRepository;
    private final EstatisticasJogadorCampeonatoRepository estatisticasJogadorCampeonatoRepository;
    private final SuspensaoRepository suspensaoRepository;

    public JogadorResponseDto criar(JogadorRequestDto dto) {
        Time time = timeRepository.findById(dto.timeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Time com id " + dto.timeId() + " não encontrado"
                        )
                );
        Jogador entity = jogadorMapper.toEntity(dto);
        entity.setTime(time);
        Jogador salvo = jogadorRepository.save(entity);
        return jogadorMapper.toDto(salvo);
    }

    public List<JogadorResponseDto> listarTodosJogadores(Pageable pageable) {
        return jogadorRepository.findAll(pageable)
                .stream()
                .map(jogadorMapper::toDto)
                .toList();
    }

    public List<JogadorResponseDto> buscarPorNome(String nome) {
        return jogadorRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(jogadorMapper::toDto)
                .toList();
    }

    public JogadorResponseDto obterJogadorPorId(Long id) {
        Jogador jogador = jogadorRepository.findById(id)
                .orElseThrow(()->
                new ResourceNotFoundException(
                        "Jogador com id " + id + " não encontrado"
                )
        );
        return jogadorMapper.toDto(jogador);
    }

    public JogadorResponseDto atualizarJogador(JogadorRequestDto dto, Long id){
        Jogador jogador = jogadorRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                "Jogador com id " + id + " não encontrado"
                        )
                );
        Time time = timeRepository.findById(dto.timeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Time com id " + dto.timeId() + " não encontrado"
                        )
                );
        jogadorMapper.updateJogadorFromDto(dto, jogador);

        jogador.setTime(time);

        Jogador jogadorAtualizado = jogadorRepository.save(jogador);

        return jogadorMapper.toDto(jogadorAtualizado);
    }

    public void deletarJogador(Long id) {
        synchronized (this) {
            Jogador jogador = jogadorRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Jogador com id " + id + " não encontrado"));

            List<String> dependencias = new ArrayList<>();

            long eventosPrincipal = eventoPartidaRepository.countByJogadorId(id);
            if (eventosPrincipal > 0) {
                dependencias.add(eventosPrincipal + " evento(s) como participante");
            }

            long eventosSecundario = eventoPartidaRepository.countByJogadorSecundarioId(id);
            if (eventosSecundario > 0) {
                dependencias.add(eventosSecundario + " evento(s) como jogador secundário");
            }

            if (estatisticasJogadorRepository.findByJogadorId(id).isPresent()) {
                dependencias.add("estatísticas de carreira");
            }

            long statsCampeonato = estatisticasJogadorCampeonatoRepository.countByJogadorId(id);
            if (statsCampeonato > 0) {
                dependencias.add(statsCampeonato + " estatística(s) em campeonato(s)");
            }

            long suspensoes = suspensaoRepository.countByJogadorId(id);
            if (suspensoes > 0) {
                dependencias.add(suspensoes + " suspensão(ões)");
            }

            if (!dependencias.isEmpty()) {
                throw new BusinessException(
                        "Não é possível excluir o jogador '" + jogador.getNome() +
                                "'. Possui: " + String.join(", ", dependencias) + "."
                );
            }

            jogadorRepository.delete(jogador);
        }
    }
}