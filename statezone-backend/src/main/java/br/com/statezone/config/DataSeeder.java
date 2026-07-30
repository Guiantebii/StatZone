package br.com.statezone.config;

import br.com.statezone.enums.*;
import br.com.statezone.model.*;
import br.com.statezone.repository.*;
import br.com.statezone.service.MatchEngine;
import br.com.statezone.service.helper.RoundRobinHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    @PersistenceContext
    private EntityManager entityManager;

    private final UsuarioRepository usuarioRepository;
    private final TimeRepository timeRepository;
    private final JogadorRepository jogadorRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final GrupoRepository grupoRepository;
    private final PartidaRepository partidaRepository;
    private final EventoPartidaRepository eventoPartidaRepository;
    private final MatchEngine matchEngine;
    private final PasswordEncoder passwordEncoder;
    private final RoundRobinHelper roundRobinHelper;
    private final PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    private final Random random = new Random(42);
    private int nameCounter = 0;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    private static final String[] FIRST_NAMES = {
            "João", "Pedro", "Lucas", "Gabriel", "Rafael", "Matheus", "Felipe", "Gustavo",
            "Marcelo", "Bruno", "Carlos", "Paulo", "André", "Diego", "Eduardo", "Fábio",
            "Fernando", "Henrique", "Igor", "José", "Leandro", "Marcos", "Ricardo", "Sérgio",
            "Thiago", "Vinícius", "Alexandre", "Antônio", "Daniel", "Edson", "Francisco",
            "Guilherme", "Hugo", "Jorge", "Luís", "Mário", "Roberto", "Samuel", "Alan",
            "Breno", "Caio", "Danilo", "Elias", "Renato", "César", "Luan", "Vitor", "Wagner"
    };

    private static final String[] LAST_NAMES = {
            "Silva", "Santos", "Oliveira", "Souza", "Lima", "Pereira", "Costa", "Rodrigues",
            "Almeida", "Nascimento", "Araújo", "Carvalho", "Gomes", "Martins", "Barbosa",
            "Ribeiro", "Cardoso", "Rocha", "Dias", "Moreira", "Teixeira", "Fernandes",
            "Mendes", "Campos", "Freitas", "Pinto", "Vieira", "Moura", "Brito", "Castro",
            "Melo", "Neves", "Monteiro", "Azevedo", "Barros", "Miranda", "Pires", "Tavares",
            "Figueiredo", "Reis", "Neto", "Machado", "Soares", "Andrade", "Batista", "Duarte"
    };

    private static final String[][] TIMES_DATA = {
            {"Flamengo", "FLA", "Rio de Janeiro", "Maracanã"},
            {"Palmeiras", "PAL", "São Paulo", "Allianz Parque"},
            {"Santos", "SAN", "Santos", "Vila Belmiro"},
            {"São Paulo", "SAO", "São Paulo", "Morumbi"},
            {"Corinthians", "COR", "São Paulo", "Neo Química Arena"},
            {"Grêmio", "GRE", "Porto Alegre", "Arena do Grêmio"},
            {"Internacional", "INT", "Porto Alegre", "Beira-Rio"},
            {"Cruzeiro", "CRU", "Belo Horizonte", "Mineirão"},
            {"Atlético-MG", "CAM", "Belo Horizonte", "Arena MRV"},
            {"Fluminense", "FLU", "Rio de Janeiro", "Maracanã"},
            {"Botafogo", "BOT", "Rio de Janeiro", "Nilton Santos"},
            {"Vasco", "VAS", "Rio de Janeiro", "São Januário"},
            {"Bahia", "BAH", "Salvador", "Arena Fonte Nova"},
            {"Fortaleza", "FOR", "Fortaleza", "Castelão"},
            {"Athletico-PR", "CAP", "Curitiba", "Ligga Arena"},
            {"Ceará", "CEA", "Fortaleza", "Castelão"},
            {"Goiás", "GOI", "Goiânia", "Serrinha"},
            {"Sport", "SPO", "Recife", "Ilha do Retiro"},
            {"Vitória", "VIT", "Salvador", "Barradão"},
            {"Coritiba", "CFC", "Curitiba", "Couto Pereira"}
    };

    private static final String[] TECNICOS = {
            "Tite", "Abel Ferreira", "Fábio Carille", "Luis Zubeldía", "Ramón Díaz",
            "Renato Gaúcho", "Roger Machado", "Fernando Diniz", "Gabriel Milito",
            "Mano Menezes", "Artur Jorge", "Rafael Paiva", "Rogério Ceni",
            "Juan Pablo Vojvoda", "Cuca", "Vagner Mancini", "Jair Ventura",
            "Marquinhos Santos", "Thiago Carpini", "Mozart"
    };

    private static final String[] ARBITROS = {
            "Wilton Pereira Sampaio", "Raphael Claus", "Anderson Daronco",
            "Bráulio da Silva Machado", "Rodrigo José Pereira de Lima",
            "Flávio Rodrigues de Souza", "Marcelo de Lima Henrique",
            "Paulo César Zanovelli", "Savio Pereira Sampaio", "Leandro Pedro Vuaden"
    };

    public void run() {
        System.out.println("=== DataSeeder: iniciando... ===");
        transactionTemplate.executeWithoutResult(s -> cleanDatabase());
        transactionTemplate.executeWithoutResult(s -> seedUsuarios());
        List<Time> times = transactionTemplate.execute(s -> seedTimes());
        Map<Long, List<Jogador>> jogadoresPorTime = transactionTemplate.execute(s -> seedJogadores(times));
        transactionTemplate.executeWithoutResult(s -> seedCampeonato1(times, jogadoresPorTime));
        transactionTemplate.executeWithoutResult(s -> seedCampeonato2(times, jogadoresPorTime));
        System.out.println("=== DataSeeder: concluído! ===");
    }

    public void cleanDatabase() {
        System.out.println("Limpando banco de dados...");
        entityManager.createNativeQuery("DELETE FROM eventos_partida").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM estatisticas_partida").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM estatisticas_jogador_campeonato").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM estatisticas_jogador").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM suspensoes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM escalacao_partida").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM processamento_confronto_pendente").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM confrontos_eliminatorios").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM partidas").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM fases_eliminatorias").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM grupo_times").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM grupos").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM campeonato_times").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM jogadores").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM campeonatos").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM times").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM refresh_tokens").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM usuarios").executeUpdate();
        System.out.println("Banco limpo.");
    }

    private void seedUsuarios() {
        Usuario admin = new Usuario();
        admin.setEmail("admin@statzone.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        usuarioRepository.save(admin);

        Usuario operador = new Usuario();
        operador.setEmail("operador@statzone.com");
        operador.setSenha(passwordEncoder.encode("oper123"));
        operador.setRole(Role.OPERADOR);
        usuarioRepository.save(operador);

        Usuario user = new Usuario();
        user.setEmail("user@statzone.com");
        user.setSenha(passwordEncoder.encode("user123"));
        user.setRole(Role.USER);
        usuarioRepository.save(user);

        System.out.println("Usuários criados: " + usuarioRepository.count());
    }

    private List<Time> seedTimes() {
        List<Time> times = new ArrayList<>();
        for (int i = 0; i < TIMES_DATA.length; i++) {
            String[] data = TIMES_DATA[i];
            Time t = new Time();
            t.setNome(data[0]);
            t.setSigla(data[1]);
            t.setTipo(TipoTime.CLUBE);
            t.setCidade(data[2]);
            t.setPais("Brasil");
            t.setEstadio(data[3]);
            t.setTecnico(TECNICOS[i]);
            t.setFundadoEm(LocalDate.of(1900 + random.nextInt(120), 1 + random.nextInt(12), 1 + random.nextInt(28)));
            times.add(timeRepository.save(t));
        }
        System.out.println("Times criados: " + times.size());
        return times;
    }

    private Map<Long, List<Jogador>> seedJogadores(List<Time> times) {
        Map<Long, List<Jogador>> mapa = new HashMap<>();
        int total = 0;
        for (Time time : times) {
            List<Jogador> jogadores = new ArrayList<>();
            int camisa = 1;

            // 2 goleiros
            for (int g = 0; g < 2; g++) {
                jogadores.add(criarJogador(time, Posicao.GOLEIRO, camisa++));
            }
            // 3 zagueiros
            for (int z = 0; z < 3; z++) {
                jogadores.add(criarJogador(time, Posicao.ZAGUEIRO, camisa++));
            }
            // 1 lateral direito
            jogadores.add(criarJogador(time, Posicao.LATERAL_DIREITO, camisa++));
            // 1 lateral esquerdo
            jogadores.add(criarJogador(time, Posicao.LATERAL_ESQUERDO, camisa++));
            // 2 volantes
            for (int v = 0; v < 2; v++) {
                jogadores.add(criarJogador(time, Posicao.VOLANTE, camisa++));
            }
            // 2 meias
            for (int m = 0; m < 2; m++) {
                jogadores.add(criarJogador(time, Posicao.MEIO_CAMPO, camisa++));
            }
            // 1 meia-atacante
            jogadores.add(criarJogador(time, Posicao.MEIA_ATACANTE, camisa++));
            // 1 ponta direita
            jogadores.add(criarJogador(time, Posicao.PONTA_DIREITA, camisa++));
            // 1 ponta esquerda
            jogadores.add(criarJogador(time, Posicao.PONTA_ESQUERDA, camisa++));
            // 1 centroavante
            jogadores.add(criarJogador(time, Posicao.CENTROAVANTE, camisa++));

            mapa.put(time.getId(), jogadorRepository.saveAll(jogadores));
            total += jogadores.size();
        }
        System.out.println("Jogadores criados: " + total);
        return mapa;
    }

    private Jogador criarJogador(Time time, Posicao posicao, int camisa) {
        String nome = FIRST_NAMES[nameCounter % FIRST_NAMES.length] + " " +
                LAST_NAMES[(nameCounter * 7 + 3) % LAST_NAMES.length];
        nameCounter++;

        Jogador j = new Jogador();
        j.setNome(nome);
        j.setTime(time);
        j.setPosicao(posicao);
        j.setNumeroCamisa(camisa);
        j.setDataNascimento(LocalDate.of(1990 + random.nextInt(15), 1 + random.nextInt(12), 1 + random.nextInt(28)));
        j.setNacionalidade("Brasil");
        j.setAltura(BigDecimal.valueOf(1.70 + random.nextDouble() * 0.30));
        j.setPeso(BigDecimal.valueOf(65 + random.nextInt(25)));
        j.setPeForte(random.nextBoolean() ? PeForte.DIREITO : PeForte.ESQUERDO);
        j.setValorMercado(BigDecimal.valueOf(1 + random.nextInt(50)).multiply(BigDecimal.valueOf(1_000_000)));
        return j;
    }

    private void seedCampeonato1(List<Time> times, Map<Long, List<Jogador>> jogadoresPorTime) {
        Campeonato br = new Campeonato();
        br.setNome("Brasileirão Série A 2025");
        br.setPais("Brasil");
        br.setTemporada("2025");
        br.setTipoFormato(TipoFormato.PONTOS_CORRIDOS);
        br.setTimes(new ArrayList<>(times));
        br.setLogoUrl("https://www.cbf.com.br/favicon.ico");
        br = campeonatoRepository.save(br);

        System.out.println("Campeonato 1: " + br.getNome());

        gerarEProcessarPontosCorridos(br, times, jogadoresPorTime);
    }

    private void seedCampeonato2(List<Time> times, Map<Long, List<Jogador>> jogadoresPorTime) {
        List<Time> timesCopa = new ArrayList<>(times.subList(0, 16));

        Campeonato copa = new Campeonato();
        copa.setNome("Copa do Brasil 2025");
        copa.setPais("Brasil");
        copa.setTemporada("2025");
        copa.setTipoFormato(TipoFormato.GRUPOS_E_MATA_MATA);
        copa.setTimes(new ArrayList<>(timesCopa));
        copa.setLogoUrl("https://www.cbf.com.br/favicon.ico");
        copa = campeonatoRepository.save(copa);

        System.out.println("Campeonato 2: " + copa.getNome());

        String[] nomesGrupos = {"A", "B", "C", "D"};
        int timesPorGrupo = 4;

        for (int g = 0; g < nomesGrupos.length; g++) {
            Grupo grupo = new Grupo();
            grupo.setCampeonato(copa);
            grupo.setNome(nomesGrupos[g]);
            List<Time> timesGrupo = new ArrayList<>();
            for (int t = 0; t < timesPorGrupo; t++) {
                timesGrupo.add(timesCopa.get(g * timesPorGrupo + t));
            }
            grupo.setTimes(timesGrupo);
            grupo = grupoRepository.save(grupo);

            gerarFixturesGrupo(grupo, copa, jogadoresPorTime);
        }
    }

    private void gerarFixturesGrupo(Grupo grupo, Campeonato campeonato, Map<Long, List<Jogador>> jogadoresPorTime) {
        List<Partida> partidas = new ArrayList<>();
        roundRobinHelper.gerarTurno(grupo.getTimes(), 1, (mandante, visitante) -> {
            Partida p = new Partida();
            p.setCampeonato(campeonato);
            p.setTimeMandante(mandante);
            p.setTimeVisitante(visitante);
            p.setGrupo(grupo);
            p.setEstadio(mandante.getEstadio());
            p.setArbitro(ARBITROS[random.nextInt(ARBITROS.length)]);
            p.setDataPartida(LocalDateTime.now().plusDays(partidas.size() + 1));
            p.setFormacaoMandante(Formacao.values()[random.nextInt(Formacao.values().length)]);
            p.setFormacaoVisitante(Formacao.values()[random.nextInt(Formacao.values().length)]);
            p.setGolsMandante(0);
            p.setGolsVisitante(0);
            p.setStatus(StatusPartida.AGENDADA);
            p = partidaRepository.save(p);
            partidas.add(p);
        });
        processarPartidas(partidas, jogadoresPorTime);
    }

    private void gerarEProcessarPontosCorridos(Campeonato campeonato, List<Time> times, Map<Long, List<Jogador>> jogadoresPorTime) {
        List<Partida> ida = gerarTurno(campeonato, times, 1);
        int totalRodadasIda = times.size() - 1;
        List<Partida> volta = new ArrayList<>();
        for (Partida p : ida) {
            Partida v = new Partida();
            v.setCampeonato(campeonato);
            v.setTimeMandante(p.getTimeVisitante());
            v.setTimeVisitante(p.getTimeMandante());
            v.setRodada(p.getRodada() + totalRodadasIda);
            v.setEstadio(v.getTimeMandante().getEstadio());
            v.setArbitro(ARBITROS[random.nextInt(ARBITROS.length)]);
            v.setDataPartida(LocalDateTime.now().plusDays(v.getRodada()));
            v.setFormacaoMandante(Formacao.values()[random.nextInt(Formacao.values().length)]);
            v.setFormacaoVisitante(Formacao.values()[random.nextInt(Formacao.values().length)]);
            v.setGolsMandante(0);
            v.setGolsVisitante(0);
            v.setStatus(StatusPartida.AGENDADA);
            v = partidaRepository.save(v);
            volta.add(v);
        }
        System.out.println("  Ida: " + ida.size() + " partidas. Volta: " + volta.size() + " partidas. Processando...");
        processarPartidas(ida, jogadoresPorTime);
        processarPartidas(volta, jogadoresPorTime);
    }

    private List<Partida> gerarTurno(Campeonato campeonato, List<Time> times, int rodadaOffset) {
        List<Partida> partidas = new ArrayList<>();
        roundRobinHelper.gerarTurno(times, rodadaOffset, (mandante, visitante) -> {
            Partida p = new Partida();
            p.setCampeonato(campeonato);
            p.setTimeMandante(mandante);
            p.setTimeVisitante(visitante);
            p.setRodada(partidas.size() + rodadaOffset);
            p.setEstadio(mandante.getEstadio());
            p.setArbitro(ARBITROS[random.nextInt(ARBITROS.length)]);
            p.setDataPartida(LocalDateTime.now().plusDays(partidas.size() + rodadaOffset));
            p.setFormacaoMandante(Formacao.values()[random.nextInt(Formacao.values().length)]);
            p.setFormacaoVisitante(Formacao.values()[random.nextInt(Formacao.values().length)]);
            p.setGolsMandante(0);
            p.setGolsVisitante(0);
            p.setStatus(StatusPartida.AGENDADA);
            p = partidaRepository.save(p);
            partidas.add(p);
        });
        return partidas;
    }

    private void processarPartidas(List<Partida> partidas, Map<Long, List<Jogador>> jogadoresPorTime) {
        int count = 0;
        int erros = 0;
        for (Partida p : partidas) {
            if (count % 50 == 0 && count > 0) {
                System.out.println("  Processadas " + count + "/" + partidas.size() + " partidas (" + erros + " erros)...");
            }
            try {
                processarPartida(p, jogadoresPorTime);
            } catch (Exception e) {
                erros++;
                System.err.println("  Erro ao processar partida " + p.getId() + ": " + e.getMessage());
            }
            count++;
        }
        System.out.println("  Partidas processadas: " + count + " (" + erros + " erros)");
    }

    private void processarPartida(Partida p, Map<Long, List<Jogador>> jogadoresPorTime) {
        int golsM = random.nextInt(6);
        int golsV = random.nextInt(5);
        p.setGolsMandante(golsM);
        p.setGolsVisitante(golsV);
        p.setStatus(StatusPartida.ENCERRADA);
        p = partidaRepository.save(p);

        List<Jogador> jogadoresM = jogadoresPorTime.get(p.getTimeMandante().getId());
        List<Jogador> jogadoresV = jogadoresPorTime.get(p.getTimeVisitante().getId());

        List<Posicao> ofensivos = List.of(Posicao.CENTROAVANTE, Posicao.PONTA_DIREITA, Posicao.PONTA_ESQUERDA, Posicao.MEIA_ATACANTE);
        List<Posicao> meias = List.of(Posicao.MEIO_CAMPO, Posicao.VOLANTE, Posicao.MEIA_ATACANTE, Posicao.MEIO_DIREITO, Posicao.MEIO_ESQUERDO);
        List<Posicao> defensivos = List.of(Posicao.ZAGUEIRO, Posicao.LATERAL_DIREITO, Posicao.LATERAL_ESQUERDO, Posicao.VOLANTE);
        List<Posicao> todos = List.of(Posicao.values());

        List<EventoPartida> eventos = new ArrayList<>();

        for (int g = 0; g < golsM; g++) {
            Jogador artilheiro = escolherJogadorPorPosicao(jogadoresM, ofensivos, meias, defensivos, 60, 25, 15);
            Jogador assistente = random.nextInt(100) < 50 ? escolherJogadorPorPosicao(jogadoresM, meias, ofensivos, defensivos, 55, 30, 15) : null;
            int minuto = 1 + random.nextInt(94);
            eventos.add(criarEvento(p, TipoEvento.GOL, artilheiro, p.getTimeMandante(), minuto, assistente));
        }

        for (int g = 0; g < golsV; g++) {
            Jogador artilheiro = escolherJogadorPorPosicao(jogadoresV, ofensivos, meias, defensivos, 60, 25, 15);
            Jogador assistente = random.nextInt(100) < 50 ? escolherJogadorPorPosicao(jogadoresV, meias, ofensivos, defensivos, 55, 30, 15) : null;
            int minuto = 1 + random.nextInt(94);
            eventos.add(criarEvento(p, TipoEvento.GOL, artilheiro, p.getTimeVisitante(), minuto, assistente));
        }

        int cartoesM = random.nextInt(4);
        for (int c = 0; c < cartoesM; c++) {
            Jogador jogador = escolherJogadorPorPosicao(jogadoresM, defensivos, meias, ofensivos, 45, 35, 20);
            TipoEvento tipo = random.nextInt(10) < 2 ? TipoEvento.CARTAO_VERMELHO : TipoEvento.CARTAO_AMARELO;
            eventos.add(criarEvento(p, tipo, jogador, p.getTimeMandante(), 1 + random.nextInt(94), null));
        }

        int cartoesV = random.nextInt(4);
        for (int c = 0; c < cartoesV; c++) {
            Jogador jogador = escolherJogadorPorPosicao(jogadoresV, defensivos, meias, ofensivos, 45, 35, 20);
            TipoEvento tipo = random.nextInt(10) < 2 ? TipoEvento.CARTAO_VERMELHO : TipoEvento.CARTAO_AMARELO;
            eventos.add(criarEvento(p, tipo, jogador, p.getTimeVisitante(), 1 + random.nextInt(94), null));
        }

        if (random.nextInt(5) < 2) {
            for (int s = 0; s < 1 + random.nextInt(3); s++) {
                Jogador sai = escolherJogadorPorPosicao(jogadoresM, todos, todos, todos, 33, 33, 34);
                Jogador entra = escolherJogadorPorPosicao(jogadoresM, todos, todos, todos, 33, 33, 34);
                if (!sai.equals(entra)) {
                    EventoPartida sub = criarEvento(p, TipoEvento.SUBSTITUICAO, sai, p.getTimeMandante(), 45 + random.nextInt(50), entra);
                    eventos.add(sub);
                }
            }
        }

        Jogador goleiroM = jogadoresM.stream().filter(j -> j.getPosicao() == Posicao.GOLEIRO).findFirst().orElse(null);
        Jogador goleiroV = jogadoresV.stream().filter(j -> j.getPosicao() == Posicao.GOLEIRO).findFirst().orElse(null);

        int defesasM = golsV > 0 ? random.nextInt(golsV + 2) : random.nextInt(3);
        int defesasV = golsM > 0 ? random.nextInt(golsM + 2) : random.nextInt(3);

        for (int d = 0; d < defesasM && goleiroM != null; d++) {
            eventos.add(criarEvento(p, TipoEvento.DEFESA, goleiroM, p.getTimeMandante(), 1 + random.nextInt(94), null));
        }
        for (int d = 0; d < defesasV && goleiroV != null; d++) {
            eventos.add(criarEvento(p, TipoEvento.DEFESA, goleiroV, p.getTimeVisitante(), 1 + random.nextInt(94), null));
        }

        if (goleiroM != null) {
            boolean convertido = random.nextBoolean();
            TipoEvento tipoPenalti = convertido ? TipoEvento.PENALTI_GOL : TipoEvento.PENALTI_DEFENDIDO;
            Jogador batedor = escolherJogadorPorPosicao(jogadoresV, ofensivos, meias, defensivos, 50, 35, 15);
            eventos.add(criarEvento(p, tipoPenalti, batedor, p.getTimeVisitante(), 1 + random.nextInt(94), convertido ? null : goleiroM));
            if (!convertido) {
                eventos.add(criarEvento(p, TipoEvento.PENALTI_PERDIDO, goleiroM, p.getTimeMandante(), 0, null));
            }
        }
        if (goleiroV != null) {
            boolean convertido = random.nextBoolean();
            TipoEvento tipoPenalti = convertido ? TipoEvento.PENALTI_GOL : TipoEvento.PENALTI_DEFENDIDO;
            Jogador batedor = escolherJogadorPorPosicao(jogadoresM, ofensivos, meias, defensivos, 50, 35, 15);
            eventos.add(criarEvento(p, tipoPenalti, batedor, p.getTimeMandante(), 1 + random.nextInt(94), convertido ? null : goleiroV));
            if (!convertido) {
                eventos.add(criarEvento(p, TipoEvento.PENALTI_PERDIDO, goleiroV, p.getTimeVisitante(), 0, null));
            }
        }

        eventoPartidaRepository.saveAll(eventos);

        if (p.getEventos() == null) {
            p.setEventos(new ArrayList<>());
        }
        p.getEventos().addAll(eventos);

        matchEngine.process(p);
    }

    private EventoPartida criarEvento(Partida partida, TipoEvento tipo, Jogador jogador, Time time, int minuto, Jogador secundario) {
        EventoPartida e = new EventoPartida();
        e.setPartida(partida);
        e.setTipoEvento(tipo);
        e.setJogador(jogador);
        e.setTime(time);
        e.setMinuto(minuto);
        e.setDescricao(tipo.name() + " - " + jogador.getNome());
        if (secundario != null) {
            e.setJogadorSecundario(secundario);
            e.setDescricao(e.getDescricao() + " (assist: " + secundario.getNome() + ")");
        }
        if (minuto > 90) {
            e.setMinutoExtra(minuto - 90);
            e.setMinuto(90);
        }
        return e;
    }

    private Jogador escolherJogadorPorPosicao(List<Jogador> jogadores, List<Posicao> preferidas, List<Posicao> secundarias, List<Posicao> raras, int pesoPreferida, int pesoSecundaria, int pesoRara) {
        int total = pesoPreferida + pesoSecundaria + pesoRara;
        int roll = random.nextInt(total);
        List<Posicao> alvo;
        if (roll < pesoPreferida) {
            alvo = preferidas;
        } else if (roll < pesoPreferida + pesoSecundaria) {
            alvo = secundarias;
        } else {
            alvo = raras;
        }
        List<Jogador> filtrados = jogadores.stream().filter(j -> alvo.contains(j.getPosicao())).toList();
        if (filtrados.isEmpty()) {
            filtrados = jogadores;
        }
        return filtrados.get(random.nextInt(filtrados.size()));
    }
}
