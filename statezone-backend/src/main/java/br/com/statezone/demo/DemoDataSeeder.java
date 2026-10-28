package br.com.statezone.demo;

import br.com.statezone.dto.campeonato.CampeonatoRequestDto;
import br.com.statezone.dto.eliminatoria.FaseEliminatoriaRequestDto;
import br.com.statezone.dto.eliminatoria.GrupoRequestDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaRequestDto;
import br.com.statezone.dto.eventoPartida.EventoPartidaRequestDto;
import br.com.statezone.dto.jogador.JogadorRequestDto;
import br.com.statezone.dto.time.TimeRequestDto;
import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.PeForte;
import br.com.statezone.enums.Posicao;
import br.com.statezone.enums.Role;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.model.Partida;
import br.com.statezone.model.TipoTime;
import br.com.statezone.model.Usuario;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.PartidaRepository;
import br.com.statezone.repository.UsuarioRepository;
import br.com.statezone.service.BracketService;
import br.com.statezone.service.CampeonatoService;
import br.com.statezone.service.EscalacaoPartidaService;
import br.com.statezone.service.EventoPartidaService;
import br.com.statezone.service.GrupoService;
import br.com.statezone.service.JogadorService;
import br.com.statezone.service.PartidaLifecycleService;
import br.com.statezone.service.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final String ESTADIO = "Arena StatZone";
    private static final String ARBITRO = "Ana Souza";

    private final UsuarioRepository usuarioRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final PartidaRepository partidaRepository;
    private final CampeonatoService campeonatoService;
    private final TimeService timeService;
    private final JogadorService jogadorService;
    private final GrupoService grupoService;
    private final BracketService bracketService;
    private final EscalacaoPartidaService escalacaoPartidaService;
    private final EventoPartidaService eventoPartidaService;
    private final PartidaLifecycleService partidaLifecycleService;
    private final TransactionTemplate transactionTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.email:demo@statzone.dev}")
    private String email;

    @Value("${app.demo.password:portfolio}")
    private String senha;

    private final Map<Long, Long> artilheiros = new HashMap<>();
    private final Map<Long, Long> goleiros = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) {
        criarConta();
        if (campeonatoRepository.count() > 0) {
            log.info("Campeonato de demonstração já existe");
            return;
        }
        log.info("Gravando campeonato de demonstração");
        criarCampeonato();
        log.info("Demonstração pronta. Login: {}", email);
    }

    private void criarConta() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        transactionTemplate.executeWithoutResult(status -> usuarioRepository.save(Usuario.builder()
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .role(Role.ADMIN)
                .build()));
    }

    private void criarCampeonato() {
        Long campeonatoId = campeonatoService.criarCampeonato(new CampeonatoRequestDto(
                "Copa StatZone",
                "Brasil",
                "2026",
                "GRUPOS_E_MATA_MATA",
                "https://ui-avatars.com/api/?name=Copa+StatZone",
                3
        )).id();
        campeonatoService.ativarCampeonato(campeonatoId);

        List<Long> times = new ArrayList<>();
        times.add(criarTime("Atlético Litoral", "ATL", "Santos", "Lia Martins", "Caio", "Marina"));
        times.add(criarTime("Norte FC", "NFC", "Belém", "Rui Pacheco", "Diego", "Helena"));
        times.add(criarTime("Serra United", "SER", "Petrópolis", "Ivo Lemos", "Bruno", "Lívia"));
        times.add(criarTime("Rio Claro", "RCL", "Rio Claro", "Nara Vidal", "Enzo", "Bia"));
        times.forEach(timeId -> campeonatoService.adicionarTime(campeonatoId, timeId));

        Long grupoId = grupoService.criarGrupo(campeonatoId, new GrupoRequestDto("Grupo A")).id();
        times.forEach(timeId -> grupoService.adicionarTime(campeonatoId, grupoId, timeId));
        grupoService.gerarFixturesPorGrupo(campeonatoId, grupoId);

        List<Long> partidas = partidaRepository.findByCampeonatoId(campeonatoId).stream()
                .map(Partida::getId)
                .toList();

        LocalDateTime agora = LocalDateTime.now();
        finalizar(partidas.get(0), agora.minusDays(6), 1, 2, 1);
        finalizar(partidas.get(1), agora.minusDays(6), 1, 1, 0);
        finalizar(partidas.get(2), agora.minusDays(3), 2, 0, 2);
        finalizar(partidas.get(3), agora.minusDays(3), 2, 1, 1);
        deixarAoVivo(partidas.get(4), agora.minusHours(1), 3);
        agendar(partidas.get(5), agora.plusDays(2), 3);

        Long faseId = bracketService.criarFase(
                campeonatoId,
                new FaseEliminatoriaRequestDto(FaseEnum.SEMIFINAL, true)
        ).id();
        bracketService.gerarPrimeiraFase(campeonatoId, faseId, 2);

        Long mataMataId = partidaRepository.findByCampeonatoId(campeonatoId).stream()
                .map(Partida::getId)
                .filter(id -> !partidas.contains(id))
                .findFirst()
                .orElseThrow();
        finalizar(mataMataId, agora.minusDays(1), 4, 2, 0);
    }

    private Long criarTime(String nome, String sigla, String cidade, String tecnico, String atacante, String goleiro) {
        Long timeId = timeService.criar(new TimeRequestDto(
                nome,
                sigla,
                TipoTime.CLUBE,
                cidade,
                "Brasil",
                null,
                tecnico,
                ESTADIO,
                LocalDate.of(1990, 1, 15)
        )).id();
        artilheiros.put(timeId, criarJogador(atacante, Posicao.CENTROAVANTE, 9, timeId));
        goleiros.put(timeId, criarJogador(goleiro, Posicao.GOLEIRO, 1, timeId));
        return timeId;
    }

    private Long criarJogador(String nome, Posicao posicao, int numero, Long timeId) {
        return jogadorService.criar(new JogadorRequestDto(
                nome,
                LocalDate.of(1998, 5, 20),
                "Brasil",
                posicao,
                numero,
                new BigDecimal("1.80"),
                new BigDecimal("75.0"),
                new BigDecimal("1000000"),
                PeForte.DIREITO,
                null,
                timeId
        )).id();
    }

    private void finalizar(Long partidaId, LocalDateTime quando, int rodada, int golsMandante, int golsVisitante) {
        preparar(partidaId, quando, rodada);
        Lados lados = lados(partidaId);
        escalar(partidaId, lados);
        partidaLifecycleService.iniciar(partidaId);
        marcar(partidaId, artilheiros.get(lados.mandanteId()), golsMandante, 12);
        marcar(partidaId, artilheiros.get(lados.visitanteId()), golsVisitante, 55);
        partidaLifecycleService.encerrar(partidaId);
    }

    private void deixarAoVivo(Long partidaId, LocalDateTime quando, int rodada) {
        preparar(partidaId, quando, rodada);
        Lados lados = lados(partidaId);
        escalar(partidaId, lados);
        partidaLifecycleService.iniciar(partidaId);
        marcar(partidaId, artilheiros.get(lados.mandanteId()), 1, 18);
    }

    private void agendar(Long partidaId, LocalDateTime quando, int rodada) {
        preparar(partidaId, quando, rodada);
    }

    private void marcar(Long partidaId, Long jogadorId, int gols, int minutoInicial) {
        for (int i = 0; i < gols; i++) {
            eventoPartidaService.registrarEvento(new EventoPartidaRequestDto(
                    TipoEvento.GOL,
                    minutoInicial + (i * 8),
                    null,
                    null,
                    jogadorId,
                    null,
                    null
            ), partidaId);
        }
    }

    private void escalar(Long partidaId, Lados lados) {
        escalarTime(partidaId, lados.mandanteId());
        escalarTime(partidaId, lados.visitanteId());
    }

    private void escalarTime(Long partidaId, Long timeId) {
        escalacaoPartidaService.adicionarJogador(partidaId, new EscalacaoPartidaRequestDto(
                goleiros.get(timeId), FuncaoEscalacao.TITULAR, Posicao.GOLEIRO, 1));
        escalacaoPartidaService.adicionarJogador(partidaId, new EscalacaoPartidaRequestDto(
                artilheiros.get(timeId), FuncaoEscalacao.TITULAR, Posicao.CENTROAVANTE, 9));
    }

    private void preparar(Long partidaId, LocalDateTime quando, int rodada) {
        transactionTemplate.executeWithoutResult(status -> {
            Partida partida = partidaRepository.findById(partidaId).orElseThrow();
            partida.setDataPartida(quando);
            partida.setRodada(rodada);
            partida.setEstadio(ESTADIO);
            partida.setArbitro(ARBITRO);
        });
    }

    private Lados lados(Long partidaId) {
        return transactionTemplate.execute(status -> {
            Partida partida = partidaRepository.findById(partidaId).orElseThrow();
            return new Lados(partida.getTimeMandante().getId(), partida.getTimeVisitante().getId());
        });
    }

    private record Lados(Long mandanteId, Long visitanteId) {
    }
}
