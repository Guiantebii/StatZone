package br.com.statezone.service;

import br.com.statezone.dto.eliminatoria.*;
import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.FaseEliminatoriaMapper;
import br.com.statezone.model.*;
import br.com.statezone.repository.*;
import br.com.statezone.service.helper.CampeonatoAccessHelper;
import br.com.statezone.service.helper.ClassificacaoStats;
import br.com.statezone.service.ranking.RankingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final CampeonatoAccessHelper campeonatoAccessHelper;


    public FaseEliminatoriaResponseDto criarFase(Long campeonatoId, FaseEliminatoriaRequestDto dto) {
        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));

        if (faseEliminatoriaRepository.existsByCampeonatoIdAndFase(campeonatoId, dto.fase())) {
            throw new br.com.statezone.exception.BusinessException("Fase " + dto.fase() + " já existe para este campeonato.");
        }

        FaseEliminatoria novaFase = new FaseEliminatoria();
        novaFase.setCampeonato(campeonato);
        novaFase.setFase(dto.fase());
        novaFase.setJogoUnico(dto.jogoUnico() != null ? dto.jogoUnico() : true);

        FaseEliminatoria salva = faseEliminatoriaRepository.save(novaFase);

        return faseEliminatoriaMapper.toDto(salva);
    }

    @Transactional(readOnly = true)
    public List<FaseEliminatoriaResponseDto> listarFases(Long campeonatoId) {
        validarVisibilidadeCampeonato(campeonatoId);
        List<FaseEliminatoria> fases = faseEliminatoriaRepository.findByCampeonatoId(campeonatoId);
        return fases.stream().map(faseEliminatoriaMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ConfrontoEliminatorioResponseDto> listarConfrontos(Long campeonatoId) {
        validarVisibilidadeCampeonato(campeonatoId);
        List<FaseEliminatoria> fases = faseEliminatoriaRepository.findByCampeonatoId(campeonatoId);
        return fases.stream()
                .flatMap(fase -> confrontoEliminatorioRepository.findByFaseIdWithDetails(fase.getId()).stream())
                .map(faseEliminatoriaMapper::toConfrontoDto)
                .toList();
    }

    public void gerarPrimeiraFase(Long campeonatoId, Long faseId, int vagasPorGrupo) {
        FaseEliminatoria fase = faseEliminatoriaRepository.findById(faseId)
                .orElseThrow(() -> new ResourceNotFoundException("Fase não encontrada"));

        if (!fase.getCampeonato().getId().equals(campeonatoId)) {
            throw new br.com.statezone.exception.BusinessException("Fase não pertence ao campeonato informado.");
        }

        if (!confrontoEliminatorioRepository.findByFaseEliminatoriaIdOrderByBracketIndexAsc(faseId).isEmpty()) {
            throw new br.com.statezone.exception.BusinessException("Esta fase já possui confrontos gerados.");
        }

        List<Grupo> grupos = grupoRepository.findByCampeonatoIdWithTimes(campeonatoId);

        List<Time> timesList = fase.getCampeonato().getTimes();
        List<Time> times = grupos.isEmpty()
                ? (timesList != null ? new ArrayList<>(timesList) : new ArrayList<>())
                : grupos.stream()
                .flatMap(g -> rankingEngine.gerarPorGrupo(g.getId()).stream().limit(vagasPorGrupo))
                .map(ClassificacaoStats::getTime)
                .collect(Collectors.toList());

        List<ConfrontoEliminatorio> confrontos = bracketEngine.gerarFaseInicial(times, fase);

        for (ConfrontoEliminatorio c : confrontos) {
            Partida p = criarPartida(fase.getCampeonato(), c.getTimeA(), c.getTimeB());
            c.setPartidaIda(p);
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

        if (!c.getFaseEliminatoria().getCampeonato().getId().equals(campeonatoId)) {
            throw new br.com.statezone.exception.BusinessException("Confronto não pertence ao campeonato informado.");
        }

        Time vencedor = bracketEngine.resolverVencedor(c);

        c.setTimeClassificado(vencedor);
        c.setStatusConfronto(StatusConfronto.ENCERRADO);
        confrontoEliminatorioRepository.save(c);

        verificarFase(c.getFaseEliminatoria());

        return faseEliminatoriaMapper.toDto(c.getFaseEliminatoria());
    }

    public void verificarFase(FaseEliminatoria fase) {
        List<ConfrontoEliminatorio> confrontos = confrontoEliminatorioRepository.findByFaseEliminatoriaIdOrderByBracketIndexAsc(fase.getId());

        boolean terminou = confrontos.stream()
                .allMatch(c -> c.getStatusConfronto() == StatusConfronto.ENCERRADO);

        if (!terminou) return;

        if (fase.getFase() == FaseEnum.SEMIFINAL) {
            gerarTerceiroLugar(fase, confrontos);
        }

        List<Time> classificados = confrontos.stream()
                .map(ConfrontoEliminatorio::getTimeClassificado)
                .filter(Objects::nonNull)
                .toList();

        if (classificados.size() < 2) return;

        FaseEnum proxima = proximaFase(fase.getFase());
        if (proxima == null) return;

        if (faseEliminatoriaRepository.existsByCampeonatoIdAndFase(fase.getCampeonato().getId(), proxima)) return;

        FaseEliminatoria nova = new FaseEliminatoria();
        nova.setCampeonato(fase.getCampeonato());
        nova.setFase(proxima);
        nova = faseEliminatoriaRepository.save(nova);

        List<ConfrontoEliminatorio> novos = bracketEngine.gerarProximaFase(classificados, nova, nova.getFase().ordinal() + 1);

        for (ConfrontoEliminatorio c : novos) {
            Partida p = criarPartida(nova.getCampeonato(), c.getTimeA(), c.getTimeB());
            c.setPartidaIda(p);
        }

        confrontoEliminatorioRepository.saveAll(novos);
    }

    private void gerarTerceiroLugar(FaseEliminatoria fase, List<ConfrontoEliminatorio> confrontos) {
        List<Time> perdedores = confrontos.stream()
                .map(c -> {
                    Time vencedor = c.getTimeClassificado();
                    if (vencedor == null) return null;
                    return vencedor.equals(c.getTimeA()) ? c.getTimeB() : c.getTimeA();
                })
                .filter(Objects::nonNull)
                .toList();

        if (perdedores.size() < 2) return;

        if (faseEliminatoriaRepository.existsByCampeonatoIdAndFase(
                fase.getCampeonato().getId(), FaseEnum.TERCEIRO_LUGAR)) return;

        FaseEliminatoria terceiroLugar = new FaseEliminatoria();
        terceiroLugar.setCampeonato(fase.getCampeonato());
        terceiroLugar.setFase(FaseEnum.TERCEIRO_LUGAR);
        terceiroLugar = faseEliminatoriaRepository.save(terceiroLugar);

        List<ConfrontoEliminatorio> novos = bracketEngine.gerarProximaFase(
                perdedores, terceiroLugar, terceiroLugar.getFase().ordinal() + 1);

        for (ConfrontoEliminatorio c : novos) {
            Partida p = criarPartida(terceiroLugar.getCampeonato(), c.getTimeA(), c.getTimeB());
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
        p.setGolsMandante(0);
        p.setGolsVisitante(0);
        p.setEstadio(c.getNome() != null ? "Estádio " + c.getNome() : null);
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

    private void validarVisibilidadeCampeonato(Long campeonatoId) {
        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));
        campeonatoAccessHelper.validarVisibilidade(campeonato);
    }
}
