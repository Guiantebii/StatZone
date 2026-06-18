package br.com.statezone.service;

import br.com.statezone.dto.eliminatoria.*;
import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.FaseEliminatoriaMapper;
import br.com.statezone.model.*;
import br.com.statezone.repository.*;
import br.com.statezone.service.helper.ClassificacaoStats;
import br.com.statezone.service.ranking.RankingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BracketService {

    private final FaseEliminatoriaRepository faseEliminatoriaRepository;
    private final ConfrontoEliminatorioRepository confrontoEliminatorioRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final PartidaRepository partidaRepository;
    private final BracketEngine bracketEngine;
    private final FaseEliminatoriaMapper faseEliminatoriaMapper;
    private final GrupoRepository grupoRepository;
    private final SuspensaoRepository suspensaoRepository;
    private final RankingEngine rankingEngine;


    public FaseEliminatoriaResponseDto criarFase(Long campeonatoId, FaseEliminatoriaRequestDto dto) {
        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));

        FaseEliminatoria novaFase = new FaseEliminatoria();
        novaFase.setCampeonato(campeonato);
        novaFase.setFase(dto.fase());

        FaseEliminatoria salva = faseEliminatoriaRepository.save(novaFase);

        return faseEliminatoriaMapper.toDto(salva);
    }

    @Transactional(readOnly = true)
    public List<FaseEliminatoriaResponseDto> listarFases(Long campeonatoId) {
        List<FaseEliminatoria> fases = faseEliminatoriaRepository.findByCampeonatoId(campeonatoId);
        return fases.stream().map(faseEliminatoriaMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ConfrontoEliminatorioResponseDto> listarConfrontos(Long campeonatoId) {
        List<FaseEliminatoria> fases = faseEliminatoriaRepository.findByCampeonatoId(campeonatoId);
        return fases.stream()
                .flatMap(fase -> confrontoEliminatorioRepository.findByFaseEliminatoriaId(fase.getId()).stream())
                .map(faseEliminatoriaMapper::toConfrontoDto)
                .toList();
    }

    public void gerarPrimeiraFase(Long campeonatoId, Long faseId, int vagasPorGrupo) {
        FaseEliminatoria fase = faseEliminatoriaRepository.findById(faseId)
                .orElseThrow(() -> new ResourceNotFoundException("Fase não encontrada"));

        List<Grupo> grupos = grupoRepository.findByCampeonatoIdWithTimes(campeonatoId);

        List<Time> times = grupos.isEmpty()
                ? new ArrayList<>(fase.getCampeonato().getTimes())
                : grupos.stream()
                .flatMap(g -> rankingEngine.gerarPorGrupo(g.getId()).stream().limit(vagasPorGrupo))
                .map(ClassificacaoStats::getTime)
                .collect(Collectors.toList());

        List<ConfrontoEliminatorio> confrontos = bracketEngine.gerarFaseInicial(times, fase);

        for (ConfrontoEliminatorio c : confrontos) {
            Partida p = criarPartida(fase.getCampeonato(), c.getTimeA(), c.getTimeB());
            c.setPartidaIda(p);
            c.setStatusConfronto(StatusConfronto.PENDENTE);
            resolverSuspensoesPendentes(p);
        }

        confrontoEliminatorioRepository.saveAll(confrontos);
    }

    private void resolverSuspensoesPendentes(Partida partida) {
        for (Time time : List.of(partida.getTimeMandante(), partida.getTimeVisitante())) {
            suspensaoRepository
                    .findByCampeonatoIdAndJogador_Time_IdAndPartidaAlvoIsNull(
                            partida.getCampeonato().getId(), time.getId())
                    .forEach(s -> {
                        s.setPartidaAlvo(partida);
                        s.setRodadaSuspensao(partida.getRodada());
                        suspensaoRepository.save(s);
                    });
        }
    }
    public FaseEliminatoriaResponseDto encerraConfronto(Long campeonatoId, Long confrontoId) {
        ConfrontoEliminatorio c = confrontoEliminatorioRepository.findById(confrontoId)
                .orElseThrow(() -> new ResourceNotFoundException("Confronto não encontrado"));

        Time vencedor = bracketEngine.resolverVencedor(c);

        c.setTimeClassificado(vencedor);
        c.setStatusConfronto(StatusConfronto.ENCERRADO);

        bracketEngine.propagarVencedor(c, vencedor);
        confrontoEliminatorioRepository.save(c);

        verificarFase(c.getFaseEliminatoria());

        return faseEliminatoriaMapper.toDto(c.getFaseEliminatoria());
    }

    public void verificarFase(FaseEliminatoria fase) {
        List<ConfrontoEliminatorio> confrontos = confrontoEliminatorioRepository.findByFaseEliminatoriaId(fase.getId());

        boolean terminou = confrontos.stream()
                .allMatch(c -> c.getStatusConfronto() == StatusConfronto.ENCERRADO);

        if (!terminou) return;

        List<Time> classificados = confrontos.stream()
                .map(ConfrontoEliminatorio::getTimeClassificado)
                .toList();

        if (classificados.size() <= 1) return;

        FaseEnum proxima = proximaFase(fase.getFase());
        if (proxima == null) return;

        FaseEliminatoria nova = new FaseEliminatoria();
        nova.setCampeonato(fase.getCampeonato());
        nova.setFase(proxima);
        nova = faseEliminatoriaRepository.save(nova);

        List<ConfrontoEliminatorio> novos = bracketEngine.gerarProximaFase(classificados, nova, fase.getFase().ordinal() + 1);

        for (ConfrontoEliminatorio c : novos) {
            Partida p = criarPartida(nova.getCampeonato(), c.getTimeA(), c.getTimeB());
            c.setPartidaIda(p);
        }

        confrontoEliminatorioRepository.saveAll(novos);
    }

    private Partida criarPartida(Campeonato c, Time a, Time b) {
        Partida p = new Partida();
        p.setCampeonato(c);
        p.setTimeMandante(a);
        p.setTimeVisitante(b);
        p.setStatus(StatusPartida.AGENDADA);
        return partidaRepository.save(p);
    }

    private FaseEnum proximaFase(FaseEnum f) {
        return switch (f) {
            case OITAVAS -> FaseEnum.QUARTAS;
            case QUARTAS -> FaseEnum.SEMIFINAL;
            case SEMIFINAL -> FaseEnum.FINAL;
            default -> null;
        };
    }
}
