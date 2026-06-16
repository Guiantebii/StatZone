package br.com.statezone.service;

import br.com.statezone.dto.eliminatoria.*;
import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.FaseEliminatoriaMapper;
import br.com.statezone.model.*;
import br.com.statezone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BracketService {

    private final FaseEliminatoriaRepository faseRepo;
    private final ConfrontoEliminatorioRepository confrontoRepo;
    private final CampeonatoRepository campeonatoRepo;
    private final PartidaRepository partidaRepo;
    private final BracketEngine engine;
    private final FaseEliminatoriaMapper faseMapper;

    public FaseEliminatoriaResponseDto criarFase(Long campeonatoId, FaseEliminatoriaRequestDto dto) {
        Campeonato campeonato = campeonatoRepo.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));

        FaseEliminatoria novaFase = new FaseEliminatoria();
        novaFase.setCampeonato(campeonato);
        novaFase.setFase(dto.fase());

        FaseEliminatoria salva = faseRepo.save(novaFase);

        return faseMapper.toDto(salva);
    }

    @Transactional(readOnly = true)
    public List<FaseEliminatoriaResponseDto> listarFases(Long campeonatoId) {
        List<FaseEliminatoria> fases = faseRepo.findByCampeonatoId(campeonatoId);
        return fases.stream().map(faseMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ConfrontoEliminatorioResponseDto> listarConfrontos(Long campeonatoId) {
        List<FaseEliminatoria> fases = faseRepo.findByCampeonatoId(campeonatoId);
        return fases.stream()
                .flatMap(fase -> confrontoRepo.findByFaseEliminatoriaId(fase.getId()).stream())
                .map(faseMapper::toConfrontoDto)
                .toList();
    }

    public void gerarPrimeiraFase(Long campeonatoId, Long faseId) {
        FaseEliminatoria fase = faseRepo.findById(faseId)
                .orElseThrow(() -> new ResourceNotFoundException("Fase não encontrada"));

        List<Time> times = new ArrayList<>(fase.getCampeonato().getTimes());

        List<ConfrontoEliminatorio> confrontos = engine.gerarFaseInicial(times, fase);

        for (ConfrontoEliminatorio c : confrontos) {
            Partida p = criarPartida(fase.getCampeonato(), c.getTimeA(), c.getTimeB());
            c.setPartidaIda(p);
            c.setStatusConfronto(StatusConfronto.PENDENTE);
        }

        confrontoRepo.saveAll(confrontos);
    }

    public FaseEliminatoriaResponseDto encerraConfronto(Long campeonatoId, Long confrontoId) {
        ConfrontoEliminatorio c = confrontoRepo.findById(confrontoId)
                .orElseThrow(() -> new ResourceNotFoundException("Confronto não encontrado"));

        Time vencedor = engine.resolverVencedor(c);

        c.setTimeClassificado(vencedor);
        c.setStatusConfronto(StatusConfronto.ENCERRADO);

        engine.propagarVencedor(c, vencedor);
        confrontoRepo.save(c);

        verificarFase(c.getFaseEliminatoria());

        return faseMapper.toDto(c.getFaseEliminatoria());
    }

    public void verificarFase(FaseEliminatoria fase) {
        List<ConfrontoEliminatorio> confrontos = confrontoRepo.findByFaseEliminatoriaId(fase.getId());

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
        nova = faseRepo.save(nova);

        List<ConfrontoEliminatorio> novos = engine.gerarProximaFase(classificados, nova, fase.getFase().ordinal() + 1);

        for (ConfrontoEliminatorio c : novos) {
            Partida p = criarPartida(nova.getCampeonato(), c.getTimeA(), c.getTimeB());
            c.setPartidaIda(p);
        }

        confrontoRepo.saveAll(novos);
    }

    private Partida criarPartida(Campeonato c, Time a, Time b) {
        Partida p = new Partida();
        p.setCampeonato(c);
        p.setTimeMandante(a);
        p.setTimeVisitante(b);
        p.setStatus(StatusPartida.AGENDADA);
        return partidaRepo.save(p);
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