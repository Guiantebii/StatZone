package br.com.statezone.service;

import br.com.statezone.dto.ClassificacaoResponseDto;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.mapper.ClassificacaoMapper;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Time;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.service.helper.ClassificacaoStats;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassificacaoService {

    private final PartidaRepository partidaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final ClassificacaoMapper classificacaoMapper;

    public List<ClassificacaoResponseDto> gerarClassificacao(Long campeonatoId){
        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Campeonato não encontrado"));

        List<Partida> partidas = partidaRepository.findByCampeonatoIdAndStatus(campeonatoId, StatusPartida.ENCERRADA);

        Map<Long, ClassificacaoStats> classificacaoMap =
                new HashMap<>();

        for (Partida partida : partidas) {

                // Pega os times da partida
                Time mandante = partida.getTimeMandante();
                Time visitante = partida.getTimeVisitante();

                // Busca as estatísticas do mandante no Map
                // Se não existir, cria uma nova entrada
                ClassificacaoStats statsMandante =
                        classificacaoMap.computeIfAbsent(
                                mandante.getId(),
                                id -> new ClassificacaoStats(mandante)
                        );

                // Busca as estatísticas do visitante no Map
                // Se não existir, cria uma nova entrada
                ClassificacaoStats statsVisitante =
                        classificacaoMap.computeIfAbsent(
                                visitante.getId(),
                                id -> new ClassificacaoStats(visitante)
                        );

                // Adiciona +1 jogo para o mandante
                statsMandante.setJogos(
                        statsMandante.getJogos() + 1
                );

                // Adiciona +1 jogo para o visitante
                statsVisitante.setJogos(
                        statsVisitante.getJogos() + 1
                );

                // Guarda os gols da partida em variáveis
                Integer golsMandante = partida.getGolsMandante();
                Integer golsVisitante = partida.getGolsVisitante();

                // Soma os gols feitos pelo mandante
                statsMandante.setGolsFeitos(
                        statsMandante.getGolsFeitos() + golsMandante
                );

                // Soma os gols sofridos pelo mandante
                statsMandante.setGolsSofridos(
                        statsMandante.getGolsSofridos() + golsVisitante
                );

                // Soma os gols feitos pelo visitante
                statsVisitante.setGolsFeitos(
                        statsVisitante.getGolsFeitos() + golsVisitante
                );

                // Soma os gols sofridos pelo visitante
                statsVisitante.setGolsSofridos(
                        statsVisitante.getGolsSofridos() + golsMandante
                );

                // Atualiza saldo de gols do mandante
                statsMandante.setSaldoGols(
                        statsMandante.getGolsFeitos()
                        - statsMandante.getGolsSofridos()
                );

                // Atualiza saldo de gols do visitante
                statsVisitante.setSaldoGols(
                        statsVisitante.getGolsFeitos()
                                - statsVisitante.getGolsSofridos()
                );

            // Verifica se o mandante venceu
            if (golsMandante > golsVisitante) {

                // Adiciona +1 vitória ao mandante
                statsMandante.setVitorias(
                        statsMandante.getVitorias() + 1
                );

                // Adiciona +3 pontos ao mandante
                statsMandante.setPontos(
                        statsMandante.getPontos() + 3
                );

                // Adiciona +1 derrota ao visitante
                statsVisitante.setDerrotas(
                        statsVisitante.getDerrotas() + 1
                );

                // Verifica se o visitante venceu
            } else if (golsVisitante > golsMandante) {

                // Adiciona +1 vitória ao visitante
                statsVisitante.setVitorias(
                        statsVisitante.getVitorias() + 1
                );

                // Adiciona +3 pontos ao visitante
                statsVisitante.setPontos(
                        statsVisitante.getPontos() + 3
                );

                // Adiciona +1 derrota ao mandante
                statsMandante.setDerrotas(
                        statsMandante.getDerrotas() + 1
                );

                // Caso contrário, a partida empatou
            } else {

                // Adiciona +1 empate ao mandante
                statsMandante.setEmpates(
                        statsMandante.getEmpates() + 1
                );

                // Adiciona +1 empate ao visitante
                statsVisitante.setEmpates(
                        statsVisitante.getEmpates() + 1
                );

                // Adiciona +1 ponto ao mandante
                statsMandante.setPontos(
                        statsMandante.getPontos() + 1
                );

                // Adiciona +1 ponto ao visitante
                statsVisitante.setPontos(
                        statsVisitante.getPontos() + 1
                );
            }

        }
        List<ClassificacaoStats> classificacaoOrdenada =
                classificacaoMap.values()
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                ClassificacaoStats::getPontos,
                                                Comparator.reverseOrder()
                                        )
                                        .thenComparing(
                                                ClassificacaoStats::getSaldoGols,
                                                Comparator.reverseOrder()
                                        )
                                        .thenComparing(
                                                ClassificacaoStats::getGolsFeitos,
                                                Comparator.reverseOrder()
                                        )
                        )
                        .collect(Collectors.toList());

        // Define a posição de cada time na tabela
        for (int i = 0; i < classificacaoOrdenada.size(); i++) {

            ClassificacaoStats stats =
                    classificacaoOrdenada.get(i);

            // i começa em 0
            // posição começa em 1
            stats.setPosicao(i + 1);
        }

        // Converte para DTO usando MapStruct
        return classificacaoOrdenada
                .stream()
                .map(classificacaoMapper::toDto)
                .toList();
    }
}
