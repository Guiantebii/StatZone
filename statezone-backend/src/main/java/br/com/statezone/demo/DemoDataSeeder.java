package br.com.statezone.demo;

import br.com.statezone.dto.campeonato.CampeonatoRequestDto;
import br.com.statezone.dto.eliminatoria.FaseEliminatoriaRequestDto;
import br.com.statezone.dto.eliminatoria.GrupoRequestDto;
import br.com.statezone.dto.escalacao.EscalacaoPartidaRequestDto;
import br.com.statezone.dto.eventoPartida.EventoPartidaRequestDto;
import br.com.statezone.dto.jogador.JogadorRequestDto;
import br.com.statezone.dto.time.TimeRequestDto;
import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.Formacao;
import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.PeForte;
import br.com.statezone.enums.Posicao;
import br.com.statezone.enums.Role;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Partida;
import br.com.statezone.model.TipoTime;
import br.com.statezone.model.Usuario;
import br.com.statezone.repository.CampeonatoRepository;
import br.com.statezone.repository.JogadorRepository;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final String NOME_COPA = "Copa StatZone";
    private static final int ELENCO_COMPLETO = 11;

    private static final List<Vaga> FORMACAO_433 = List.of(
            new Vaga(Posicao.GOLEIRO, 1, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.LATERAL_ESQUERDO, 6, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.ZAGUEIRO, 4, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.ZAGUEIRO, 3, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.LATERAL_DIREITO, 2, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.VOLANTE, 5, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.MEIO_CAMPO, 8, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.MEIO_CAMPO, 10, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.PONTA_ESQUERDA, 11, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.CENTROAVANTE, 9, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.PONTA_DIREITA, 7, FuncaoEscalacao.TITULAR),
            new Vaga(Posicao.ZAGUEIRO, 14, FuncaoEscalacao.RESERVA),
            new Vaga(Posicao.MEIO_CAMPO, 15, FuncaoEscalacao.RESERVA),
            new Vaga(Posicao.CENTROAVANTE, 17, FuncaoEscalacao.RESERVA)
    );

    private static final String ESTADIO = "Arena StatZone";
    private static final String ARBITRO = "Ana Souza";

    private final UsuarioRepository usuarioRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final JogadorRepository jogadorRepository;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.demo.email:demo@statzone.dev}")
    private String email;

    @Value("${app.demo.password:portfolio}")
    private String senha;

    private final Map<Long, List<JogadorEscalado>> elencos = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) {
        criarConta();
        if (copaCompleta()) {
            log.info("Copa StatZone já tem elenco completo");
            return;
        }
        apagarCopaIncompleta();
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
        times.add(criarTime("Atlético Litoral", "ATL", "Santos", "Lia Martins",
                List.of("Marina", "Léo", "Caíque", "Pedro", "André", "Tiago", "Rafael", "Davi", "Igor", "Caio", "Vini", "João", "Lucas", "Mateus")));
        times.add(criarTime("Norte FC", "NFC", "Belém", "Rui Pacheco",
                List.of("Helena", "Otávio", "Samuel", "Iago", "Cauã", "Heitor", "Murilo", "Gael", "Theo", "Diego", "Benício", "Arthur", "Davi L.", "Noah")));
        times.add(criarTime("Serra United", "SER", "Petrópolis", "Ivo Lemos",
                List.of("Lívia", "Bento", "Miguel", "Henrique", "Gustavo", "Lorenzo", "Bruno", "Anthony", "Joaquim", "Isaac", "Ravi", "Théo", "Nicolas", "Benjamin")));
        times.add(criarTime("Rio Claro", "RCL", "Rio Claro", "Nara Vidal",
                List.of("Bia", "Vicente", "Pietro", "Bryan", "Caleb", "Enzo", "Levi", "Ryan", "Daniel", "Kauê", "Gael R.", "Erick", "Luiz", "Oliver")));
        times.forEach(timeId -> campeonatoService.adicionarTime(campeonatoId, timeId));

        Long grupoId = grupoService.criarGrupo(campeonatoId, new GrupoRequestDto("Grupo A")).id();
        times.forEach(timeId -> grupoService.adicionarTime(campeonatoId, grupoId, timeId));
        grupoService.gerarFixturesPorGrupo(campeonatoId, grupoId);

        List<Long> partidas = partidaRepository.findByCampeonatoId(campeonatoId).stream()
                .map(Partida::getId)
                .toList();

        LocalDateTime agora = LocalDateTime.now();
        finalizar(partidas.get(0), agora.minusDays(6), 1, 2, 1, true);
        finalizar(partidas.get(1), agora.minusDays(6), 1, 1, 0, false);
        finalizar(partidas.get(2), agora.minusDays(3), 2, 0, 2, false);
        finalizar(partidas.get(3), agora.minusDays(3), 2, 1, 1, false);
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
        finalizar(mataMataId, agora.minusDays(1), 4, 2, 0, false);
    }

    private Long criarTime(String nome, String sigla, String cidade, String tecnico, List<String> nomes) {
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
        List<JogadorEscalado> elenco = new ArrayList<>();
        for (int i = 0; i < FORMACAO_433.size(); i++) {
            Vaga vaga = FORMACAO_433.get(i);
            Long jogadorId = criarJogador(nomes.get(i), vaga.posicao(), vaga.numero(), timeId);
            elenco.add(new JogadorEscalado(jogadorId, vaga.posicao(), vaga.numero(), vaga.funcao()));
        }
        elencos.put(timeId, elenco);
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

    private void finalizar(Long partidaId, LocalDateTime quando, int rodada, int golsMandante, int golsVisitante, boolean detalhes) {
        preparar(partidaId, quando, rodada);
        Lados lados = lados(partidaId);
        escalar(partidaId, lados);
        partidaLifecycleService.iniciar(partidaId);
        if (detalhes && golsMandante > 0) {
            marcarGol(partidaId, porPosicao(lados.mandanteId(), Posicao.CENTROAVANTE),
                    porPosicao(lados.mandanteId(), Posicao.PONTA_ESQUERDA), 12);
            marcar(partidaId, porPosicao(lados.mandanteId(), Posicao.CENTROAVANTE), golsMandante - 1, 28);
            eventoPartidaService.registrarEvento(new EventoPartidaRequestDto(
                    TipoEvento.CARTAO_AMARELO, 40, null, null,
                    porPosicao(lados.visitanteId(), Posicao.ZAGUEIRO), null, null
            ), partidaId);
        } else {
            marcar(partidaId, porPosicao(lados.mandanteId(), Posicao.CENTROAVANTE), golsMandante, 12);
        }
        marcar(partidaId, porPosicao(lados.visitanteId(), Posicao.CENTROAVANTE), golsVisitante, 55);
        partidaLifecycleService.encerrar(partidaId);
    }

    private void deixarAoVivo(Long partidaId, LocalDateTime quando, int rodada) {
        preparar(partidaId, quando, rodada);
        Lados lados = lados(partidaId);
        escalar(partidaId, lados);
        partidaLifecycleService.iniciar(partidaId);
        marcar(partidaId, porPosicao(lados.mandanteId(), Posicao.CENTROAVANTE), 1, 18);
    }

    private void agendar(Long partidaId, LocalDateTime quando, int rodada) {
        preparar(partidaId, quando, rodada);
    }

    private void marcarGol(Long partidaId, Long jogadorId, Long assistenteId, int minuto) {
        eventoPartidaService.registrarEvento(new EventoPartidaRequestDto(
                TipoEvento.GOL, minuto, null, null, jogadorId, assistenteId, null
        ), partidaId);
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
        for (JogadorEscalado jogador : elencos.get(timeId)) {
            escalacaoPartidaService.adicionarJogador(partidaId, new EscalacaoPartidaRequestDto(
                    jogador.id(), jogador.funcao(), jogador.posicao(), jogador.numero()));
        }
    }

    private void preparar(Long partidaId, LocalDateTime quando, int rodada) {
        transactionTemplate.executeWithoutResult(status -> {
            Partida partida = partidaRepository.findById(partidaId).orElseThrow();
            partida.setDataPartida(quando);
            partida.setRodada(rodada);
            partida.setEstadio(ESTADIO);
            partida.setArbitro(ARBITRO);
            partida.setFormacaoMandante(Formacao._4_3_3);
            partida.setFormacaoVisitante(Formacao._4_3_3);
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

    private boolean copaCompleta() {
        Boolean completa = transactionTemplate.execute(status -> copas().stream().anyMatch(copa -> {
            List<br.com.statezone.model.Time> times = copa.getTimes();
            return times != null
                    && !times.isEmpty()
                    && times.stream().allMatch(time -> jogadorRepository.countByTimeId(time.getId()) >= ELENCO_COMPLETO);
        }));
        return Boolean.TRUE.equals(completa);
    }

    private void apagarCopaIncompleta() {
        List<Long> ids = transactionTemplate.execute(status ->
                copas().stream().map(Campeonato::getId).toList());
        if (ids == null || ids.isEmpty()) {
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.clear();
            ids.forEach(this::apagarCampeonato);
            entityManager.clear();
        });
    }

    private List<Campeonato> copas() {
        return campeonatoRepository.findByNomeContainingIgnoreCase(NOME_COPA).stream()
                .filter(copa -> NOME_COPA.equals(copa.getNome()))
                .toList();
    }

    private void apagarCampeonato(Long id) {
        executar(id, """
                UPDATE eventos_partida SET evento_relacionado_id = NULL
                WHERE partida_id IN (SELECT id FROM partidas WHERE campeonato_id = :id)
                """);
        executar(id, """
                UPDATE confrontos_eliminatorios
                SET proximo_confronto_id = NULL, partida_ida_id = NULL, partida_volta_id = NULL
                WHERE fase_eliminatoria_id IN (SELECT id FROM fases_eliminatorias WHERE campeonato_id = :id)
                """);
        executar(id, """
                DELETE FROM processamento_confronto_pendente
                WHERE partida_id IN (SELECT id FROM partidas WHERE campeonato_id = :id)
                """);
        executar(id, "DELETE FROM eventos_partida WHERE partida_id IN (SELECT id FROM partidas WHERE campeonato_id = :id)");
        executar(id, "DELETE FROM escalacao_partida WHERE partida_id IN (SELECT id FROM partidas WHERE campeonato_id = :id)");
        executar(id, "DELETE FROM estatisticas_partida WHERE partida_id IN (SELECT id FROM partidas WHERE campeonato_id = :id)");
        executar(id, "DELETE FROM estatisticas_jogador_campeonato WHERE campeonato_id = :id");
        executar(id, "DELETE FROM suspensoes WHERE campeonato_id = :id");
        executar(id, """
                DELETE FROM confrontos_eliminatorios
                WHERE fase_eliminatoria_id IN (SELECT id FROM fases_eliminatorias WHERE campeonato_id = :id)
                """);
        executar(id, "DELETE FROM fases_eliminatorias WHERE campeonato_id = :id");
        executar(id, "DELETE FROM partidas WHERE campeonato_id = :id");

        @SuppressWarnings("unchecked")
        List<Number> timesExclusivos = entityManager.createNativeQuery("""
                SELECT time_id FROM campeonato_times WHERE campeonato_id = :id
                AND time_id NOT IN (SELECT time_id FROM campeonato_times WHERE campeonato_id <> :id)
                """).setParameter("id", id).getResultList();
        if (!timesExclusivos.isEmpty()) {
            String ids = timesExclusivos.stream().map(Number::longValue).map(String::valueOf).collect(Collectors.joining(","));
            entityManager.createNativeQuery("DELETE FROM estatisticas_jogador WHERE jogador_id IN (SELECT id FROM jogadores WHERE time_id IN (" + ids + "))").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM jogadores WHERE time_id IN (" + ids + ")").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM campeonato_times WHERE time_id IN (" + ids + ")").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM grupo_times WHERE time_id IN (" + ids + ")").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM times WHERE id IN (" + ids + ")").executeUpdate();
        }

        executar(id, "DELETE FROM grupo_times WHERE grupo_id IN (SELECT id FROM grupos WHERE campeonato_id = :id)");
        executar(id, "DELETE FROM grupos WHERE campeonato_id = :id");
        executar(id, "DELETE FROM campeonato_times WHERE campeonato_id = :id");
        executar(id, "DELETE FROM campeonatos WHERE id = :id");
    }

    private void executar(Long id, String sql) {
        entityManager.createNativeQuery(sql).setParameter("id", id).executeUpdate();
    }

    private Long porPosicao(Long timeId, Posicao posicao) {
        return elencos.get(timeId).stream()
                .filter(jogador -> jogador.posicao() == posicao && jogador.funcao() == FuncaoEscalacao.TITULAR)
                .map(JogadorEscalado::id)
                .findFirst()
                .orElseThrow();
    }

    private record Vaga(Posicao posicao, int numero, FuncaoEscalacao funcao) {
    }

    private record JogadorEscalado(Long id, Posicao posicao, int numero, FuncaoEscalacao funcao) {
    }
}
