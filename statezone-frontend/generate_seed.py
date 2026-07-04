#!/usr/bin/env python3
"""
Gerador de seed massivo para testar o StateZone.
Gera um arquivo SQL com ~20 times, ~300 jogadores, 380+ partidas, eventos, etc.
"""

import random
from datetime import datetime, timedelta

random.seed(42)

# ============================================================
# CONFIG
# ============================================================
NUM_TEAMS = 20
PLAYERS_PER_TEAM = 15  # 1 GK + 5 DEF + 5 MID + 4 FWD
TOTAL_PLAYERS = NUM_TEAMS * PLAYERS_PER_TEAM

# Times do Brasileirão Série A (dados realistas)
TEAMS = [
    {"nome": "Flamengo",     "sigla": "FLA", "cidade": "Rio de Janeiro",    "estadio": "Maracanã",            "pais": "Brasil"},
    {"nome": "Palmeiras",    "sigla": "PAL", "cidade": "São Paulo",         "estadio": "Allianz Parque",      "pais": "Brasil"},
    {"nome": "São Paulo",    "sigla": "SAO", "cidade": "São Paulo",         "estadio": "Morumbis",            "pais": "Brasil"},
    {"nome": "Corinthians",  "sigla": "COR", "cidade": "São Paulo",         "estadio": "Neo Química Arena",   "pais": "Brasil"},
    {"nome": "Grêmio",       "sigla": "GRE", "cidade": "Porto Alegre",      "estadio": "Arena do Grêmio",     "pais": "Brasil"},
    {"nome": "Internacional","sigla": "INT", "cidade": "Porto Alegre",      "estadio": "Beira-Rio",           "pais": "Brasil"},
    {"nome": "Santos",       "sigla": "SAN", "cidade": "Santos",            "estadio": "Vila Belmiro",        "pais": "Brasil"},
    {"nome": "Cruzeiro",     "sigla": "CRU", "cidade": "Belo Horizonte",    "estadio": "Mineirão",            "pais": "Brasil"},
    {"nome": "Atlético-MG",  "sigla": "CAM", "cidade": "Belo Horizonte",    "estadio": "Arena MRV",           "pais": "Brasil"},
    {"nome": "Botafogo",     "sigla": "BOT", "cidade": "Rio de Janeiro",    "estadio": "Nilton Santos",       "pais": "Brasil"},
    {"nome": "Fluminense",   "sigla": "FLU", "cidade": "Rio de Janeiro",    "estadio": "Maracanã",            "pais": "Brasil"},
    {"nome": "Vasco da Gama","sigla": "VAS", "cidade": "Rio de Janeiro",    "estadio": "São Januário",        "pais": "Brasil"},
    {"nome": "Bahia",        "sigla": "BAH", "cidade": "Salvador",          "estadio": "Arena Fonte Nova",    "pais": "Brasil"},
    {"nome": "Athletico-PR", "sigla": "CAP", "cidade": "Curitiba",          "estadio": "Ligga Arena",         "pais": "Brasil"},
    {"nome": "Bragantino",   "sigla": "RBB", "cidade": "Bragança Paulista", "estadio": "Nabi Abi Chedid",     "pais": "Brasil"},
    {"nome": "Fortaleza",    "sigla": "FOR", "cidade": "Fortaleza",         "estadio": "Arena Castelão",      "pais": "Brasil"},
    {"nome": "Ceará",        "sigla": "CEA", "cidade": "Fortaleza",         "estadio": "Arena Castelão",      "pais": "Brasil"},
    {"nome": "Goiás",        "sigla": "GOI", "cidade": "Goiânia",           "estadio": "Serra Dourada",       "pais": "Brasil"},
    {"nome": "Sport Recife", "sigla": "SPO", "cidade": "Recife",            "estadio": "Ilha do Retiro",      "pais": "Brasil"},
    {"nome": "Vitória",      "sigla": "VIT", "cidade": "Salvador",          "estadio": "Barradão",            "pais": "Brasil"},
]

POSITIONS = [
    "GOLEIRO", "ZAGUEIRO", "LATERAL_DIREITO", "LATERAL_ESQUERDO", "ZAGUEIRO",
    "VOLANTE", "MEIO_CAMPO", "MEIA_ATACANTE", "VOLANTE", "MEIO_CAMPO",
    "PONTA_DIREITA", "PONTA_ESQUERDA", "MEIA_ATACANTE", "CENTROAVANTE", "CENTROAVANTE",
]

NACIONALIDADES = [
    "Brasil", "Argentina", "Uruguai", "Paraguai", "Chile",
    "Colombia", "Equador", "Venezuela", "Peru", "Bolivia",
]

ARBITROS = [
    "Anderson Daronco", "Wilton Sampaio", "Raphael Claus", "Bráulio Machado",
    "Ramon Abatti", "Rafael Klein", "Savio Pereira", "Rodrigo D Alonso",
    "Caio Max Augusto", "Paulo Cesar Zanovelli", "Flavio Rodrigues de Souza",
    "Marcelo de Lima Henrique", "Bruno Arleu de Araujo", "Luiz Flavio de Oliveira",
    "Leandro Pedro Vuaden", "Wagner do Nascimento Magalhaes",
]

EVENT_TYPES = ["GOL", "GOL", "GOL", "CARTAO_AMARELO", "CARTAO_AMARELO",
               "CARTAO_VERMELHO", "SUBSTITUICAO", "FINALIZACAO", "FALTA", "ESCANTEIO",
               "IMPEDIMENTO", "DEFESA", "FINALIZACAO_NO_GOL"]

PRIMEIROS_NOMES_MASC = [
    "João", "Pedro", "Lucas", "Gabriel", "Rafael", "Felipe", "Gustavo", "Marcos",
    "Bruno", "Diego", "Thiago", "Carlos", "Eduardo", "André", "Alexandre", "Ricardo",
    "Vinicius", "Matheus", "Leonardo", "Rodrigo", "Fernando", "Paulo", "Sérgio",
    "Marcelo", "Leandro", "Luis", "Antonio", "Fabio", "Igor", "Tiago", "Daniel",
    "William", "José", "Roberto", "Márcio", "Alessandro", "Julio", "Renato", "Adriano",
    "Luciano", "Evandro", "Cristiano", "Alex", "Douglas", "Samuel", "Emerson",
    "Wellington", "Davi", "Miguel", "Arthur", "Bernardo", "Heitor", "Enzo", "Nicolas",
    "Ryan", "Cauã", "Otávio", "Breno", "Erick", "Jorge"
]

SOBRENOMES = [
    "Silva", "Santos", "Oliveira", "Souza", "Lima", "Costa", "Pereira", "Carvalho",
    "Almeida", "Rodrigues", "Martins", "Barbosa", "Araujo", "Gomes", "Ribeiro",
    "Ferreira", "Alves", "Moreira", "Nascimento", "Vieira", "Monteiro", "Cardoso",
    "Teixeira", "Dias", "Melo", "Cavalcanti", "Correia", "Mendes", "Nunes", "Ramos",
    "Pires", "Campos", "Freitas", "Machado", "Fernandes", "Barros", "Soares",
    "Azevedo", "Farias", "Castro", "Lopes", "Miranda", "Peixoto", "Aragão",
    "Figueiredo", "Guimarães", "Vargas", "Moraes", "Baptista", "Xavier", "Neves",
    "Delgado", "Rocha", "Assunção", "Chagas", "Bezerra", "Muniz", "Wagner",
    "Schmidt", "Müller", "Martínez", "García", "López", "Fernández", "González",
    "Pérez", "Romero", "Torres", "Acosta", "Medina", "Ramos",
    "Suárez", "Gómez", "Díaz", "Álvarez", "Ruiz", "Ortiz", "Morales",
]

def gen_player_name():
    return f"{random.choice(PRIMEIROS_NOMES_MASC)} {random.choice(SOBRENOMES)}"

def gen_player_names(count):
    """Generate unique player names."""
    names = set()
    while len(names) < count:
        names.add(gen_player_name())
    return list(names)

def generate_seed():
    lines = []
    out = lines.append

    out("-- ============================================================")
    out("-- SEED MASSIVO PARA TESTE — StateZone")
    out(f"-- Gerado em: {datetime.now().isoformat()}")
    out(f"-- {NUM_TEAMS} times, {TOTAL_PLAYERS} jogadores, campeonatos, partidas, eventos")
    out("-- ============================================================")
    out("")

    # ---- TRUNCATE ----
    out("-- LIMPA DADOS EXISTENTES")
    out("TRUNCATE TABLE public.suspensoes CASCADE;")
    out("TRUNCATE TABLE public.processamento_confronto_pendente CASCADE;")
    out("TRUNCATE TABLE public.estatisticas_jogador_campeonato CASCADE;")
    out("TRUNCATE TABLE public.estatisticas_jogador CASCADE;")
    out("TRUNCATE TABLE public.estatisticas_partida CASCADE;")
    out("TRUNCATE TABLE public.eventos_partida CASCADE;")
    out("TRUNCATE TABLE public.escalacao_partida CASCADE;")
    out("TRUNCATE TABLE public.confrontos_eliminatorios CASCADE;")
    out("TRUNCATE TABLE public.fases_eliminatorias CASCADE;")
    out("TRUNCATE TABLE public.grupo_times CASCADE;")
    out("TRUNCATE TABLE public.grupos CASCADE;")
    out("TRUNCATE TABLE public.partidas CASCADE;")
    out("TRUNCATE TABLE public.campeonato_times CASCADE;")
    out("TRUNCATE TABLE public.jogadores CASCADE;")
    out("TRUNCATE TABLE public.times CASCADE;")
    out("TRUNCATE TABLE public.campeonatos CASCADE;")
    out("TRUNCATE TABLE public.usuarios CASCADE;")
    out("")

    out("-- RESETA SEQUENCES")
    for seq in ["usuarios", "campeonatos", "times", "jogadores", "partidas",
                "eventos_partida", "escalacao_partida", "estatisticas_partida",
                "grupos", "fases_eliminatorias", "confrontos_eliminatorios",
                "estatisticas_jogador", "estatisticas_jogador_campeonato"]:
        out(f"SELECT setval('public.{seq}_id_seq', 1, false);")
    out("")

    # ---- ADMIN (id 1) ----
    out("-- ============================================================")
    out("-- 1. ADMIN")
    out("-- ============================================================")
    out("INSERT INTO public.usuarios (id, email, role, senha) VALUES")
    out("  (1, 'admin@statezone.com', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');")
    out("")

    # ---- TEAMS (ids 1..20) ----
    out("-- ============================================================")
    out("-- 2. TIMES")
    out("-- ============================================================")
    team_ids = list(range(1, NUM_TEAMS + 1))
    out("INSERT INTO public.times (id, nome, sigla, pais, cidade, estadio, escudo_url, seed) VALUES")
    team_rows = []
    for i, t in enumerate(TEAMS, 1):
        seed_val = (i * 7) % 100
        team_rows.append(f"  ({i}, '{t['nome']}', '{t['sigla']}', '{t['pais']}', '{t['cidade']}', '{t['estadio']}', NULL, {seed_val})")
    out(",\n".join(team_rows) + ";")
    out("")

    # ---- JOGADORES (ids 1..300) ----
    out("-- ============================================================")
    out("-- 3. JOGADORES (15 por time = 300)")
    out("-- ============================================================")
    player_id = 0
    player_rows = []
    for team_idx in range(NUM_TEAMS):
        team_id = team_idx + 1
        names = gen_player_names(PLAYERS_PER_TEAM)
        for p_idx in range(PLAYERS_PER_TEAM):
            player_id += 1
            nome = names[p_idx].replace("'", "''")
            posicao = POSITIONS[p_idx]
            numero = random.randint(1, 99)
            nacionalidade = random.choice(NACIONALIDADES)
            player_rows.append(
                f"  ({player_id}, '{nome}', '{posicao}', {numero}, {team_id}, '{nacionalidade}')"
            )

    out("INSERT INTO public.jogadores (id, nome, posicao, numero_camisa, time_id, nacionalidade) VALUES")
    out(",\n".join(player_rows) + ";")
    out("")

    # ---- CAMPEONATO 1: Brasileirão Série A 2025 (PONTOS_CORRIDOS) ----
    out("-- ============================================================")
    out("-- 4. CAMPEONATO 1: Brasileirão Série A 2025 (PONTOS_CORRIDOS)")
    out("-- ============================================================")
    out("INSERT INTO public.campeonatos (id, nome, pais, temporada, tipo_formato, logo_url, amarelos_para_suspensao) VALUES")
    out("  (1, 'Brasileirão Série A 2025', 'Brasil', '2025', 'PONTOS_CORRIDOS', NULL, 3);")
    out("")

    out("INSERT INTO public.campeonato_times (campeonato_id, time_id) VALUES")
    ct_rows = [f"  (1, {tid})" for tid in team_ids]
    out(",\n".join(ct_rows) + ";")
    out("")

    # ---- CAMPEONATO 2: Copa do Brasil 2025 (GRUPOS_E_MATA_MATA) ----
    out("-- ============================================================")
    out("-- 5. CAMPEONATO 2: Copa do Brasil 2025 (GRUPOS_E_MATA_MATA)")
    out("-- ============================================================")
    out("INSERT INTO public.campeonatos (id, nome, pais, temporada, tipo_formato, logo_url, amarelos_para_suspensao) VALUES")
    out("  (2, 'Copa do Brasil 2025', 'Brasil', '2025', 'GRUPOS_E_MATA_MATA', NULL, 3);")
    out("")

    # Only 16 teams for Copa do Brasil
    copa_team_ids = team_ids[:16]
    out("INSERT INTO public.campeonato_times (campeonato_id, time_id) VALUES")
    ct_rows2 = [f"  (2, {tid})" for tid in copa_team_ids]
    out(",\n".join(ct_rows2) + ";")
    out("")

    # ---- GRUPOS (Copa do Brasil: 4 grupos de 4 times) ----
    out("-- ============================================================")
    out("-- 6. GRUPOS (Copa do Brasil)")
    out("-- ============================================================")
    grupo_nomes = ["A", "B", "C", "D"]
    out("INSERT INTO public.grupos (id, campeonato_id, nome) VALUES")
    grupo_rows = []
    for gi, gn in enumerate(grupo_nomes, 1):
        grupo_rows.append(f"  ({gi}, 2, 'Grupo {gn}')")
    out(",\n".join(grupo_rows) + ";")
    out("")

    # Assign 4 teams per group
    out("INSERT INTO public.grupo_times (grupo_id, time_id) VALUES")
    gt_rows = []
    for gi in range(4):
        for t in range(4):
            time_id = copa_team_ids[gi * 4 + t]
            gt_rows.append(f"  ({gi + 1}, {time_id})")
    out(",\n".join(gt_rows) + ";")
    out("")

    # ---- PARTIDAS ----
    out("-- ============================================================")
    out("-- 7. PARTIDAS")
    out("-- ============================================================")
    out("-- Gerando 380 partidas do Brasileirão (38 rodadas, 10 jogos cada)")
    out("-- + 24 partidas de grupos da Copa do Brasil")
    out("")

    partida_id = 0
    partida_rows = []

    # ---- GERAR PARTIDAS DO BRASILEIRÃO (round-robin) ----
    # Algoritmo round-robin: fixa o primeiro time, rotaciona os demais
    def round_robin_fixtures(teams):
        """Generate round-robin fixtures. Returns list of (home, away) per round."""
        n = len(teams)
        if n % 2 == 1:
            teams = teams + [None]  # bye
            n += 1
        rounds = []
        for r in range(n - 1):
            matches = []
            for i in range(n // 2):
                home = teams[i]
                away = teams[n - 1 - i]
                if home is not None and away is not None:
                    # Alternate home/away
                    if r % 2 == 0:
                        matches.append((home, away))
                    else:
                        matches.append((away, home))
            rounds.append(matches)
            # Rotate: fix first, rotate rest clockwise
            teams = [teams[0]] + [teams[-1]] + teams[1:-1]
        return rounds

    # First half (turno)
    first_half = round_robin_fixtures(team_ids)
    # Second half (returno) - invert home/away
    second_half = []
    for rnd in first_half:
        second_half.append([(away, home) for (home, away) in rnd])

    all_rounds = first_half + second_half  # 38 rounds

    data_inicio = datetime(2025, 3, 29)  # Start of Brazilian season

    for round_idx, matches in enumerate(all_rounds):
        rodada = round_idx + 1
        # 3-4 matches on Saturday, 6-7 on Sunday
        for match_idx, (home, away) in enumerate(matches):
            partida_id += 1
            status = "ENCERRADA" if round_idx < 30 else "AGENDADA"
            if round_idx == 30:
                status = "AO_VIVO" if match_idx < 3 else "AGENDADA"
            if round_idx == 31:
                status = "AGENDADA"

            if match_idx < 4:
                # Saturday matches
                dia = data_inicio + timedelta(days=round_idx * 7 + 0)
                hora = f"{16 + match_idx}:00:00"
            else:
                # Sunday matches
                dia = data_inicio + timedelta(days=round_idx * 7 + 1)
                hora = f"{11 + (match_idx - 4) * 2}:00:00"

            data_partida = dia.strftime("%Y-%m-%d") + " " + hora

            if status == "ENCERRADA":
                # Generate realistic scores
                gols_m = random.choices([0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 4], weights=[15, 15, 20, 20, 10, 10, 5, 5, 3, 2, 1])[0]
                gols_v = random.choices([0, 0, 1, 1, 1, 2, 2, 3, 4], weights=[20, 15, 20, 15, 10, 10, 5, 3, 2])[0]
            else:
                gols_m = 0
                gols_v = 0

            estadio = TEAMS[home - 1]["estadio"]
            arbitro = random.choice(ARBITROS)

            partida_rows.append(
                f"  ({partida_id}, 1, {home}, {away}, '{data_partida}', '{estadio}', '{arbitro}', {rodada}, '{status}', {gols_m}, {gols_v})"
            )

    # ---- GERAR PARTIDAS DOS GRUPOS (Copa do Brasil) ----
    # 4 grupos, 4 times cada, 6 rodadas = 24 partidas por grupo = 96 partidas
    # Vamos simplificar: 3 rodadas (turno único) = 6 partidas por grupo = 24
    grupo_match_id_start = partida_id + 1
    for gi in range(4):
        group_team_ids = copa_team_ids[gi * 4:gi * 4 + 4]
        gf = round_robin_fixtures(group_team_ids)
        for round_idx, matches in enumerate(gf[:3]):  # Apenas turno
            for home, away in matches:
                partida_id += 1
                data_partida = f"2025-05-{10 + gi * 3 + round_idx:02d} {14 + (home % 3):02d}:00:00"
                estadio = TEAMS[home - 1]["estadio"]
                arbitro = random.choice(ARBITROS)
                partida_rows.append(
                    f"  ({partida_id}, 2, {home}, {away}, '{data_partida}', '{estadio}', '{arbitro}', {round_idx + 1}, 'AGENDADA', 0, 0)"
                )

    out("INSERT INTO public.partidas (id, campeonato_id, time_mandante_id, time_visitante_id, data_partida, estadio, arbitro, rodada, status, gols_mandante, gols_visitante) VALUES")
    out(",\n".join(partida_rows) + ";")
    out("")

    # ---- EVENTOS ----
    # Gerar eventos para ~50 partidas encerradas
    out("-- ============================================================")
    out("-- 8. EVENTOS DAS PARTIDAS")
    out("-- ============================================================")
    evento_id = 0
    evento_rows = []

    # Selecionar primeiras 50 partidas encerradas
    event_match_ids = list(range(1, min(51, partida_id + 1)))

    for pm_id in event_match_ids:
        # Find home_team, away_team, gols_m, gols_v from partida_rows
        # Partida row: (id, camp, home, away, data, estadio, arbitro, rodada, status, gols_m, gols_v)
        # Let me parse the data from match data
        home_team = None
        away_team = None
        gols_m = 0
        gols_v = 0

        # Actually, let's fetch from our match data more carefully
        # Since we're streaming lines, let's store match info keyed by id
        # But actually we're building everything sequentially, so I'll compute on the fly
        # Let me just reference what we know
        if pm_id <= 380:
            # Brasileirão match
            round_idx = (pm_id - 1) // 10
            match_in_round = (pm_id - 1) % 10
            matches_in_round = all_rounds[round_idx]
            if match_in_round < len(matches_in_round):
                home_team, away_team = matches_in_round[match_in_round]
            gols_m = random.choices([0, 0, 1, 1, 2, 2, 3, 3, 4], weights=[15, 10, 25, 15, 15, 10, 5, 3, 2])[0]
            gols_v = random.choices([0, 0, 1, 1, 2, 2, 3], weights=[25, 15, 25, 15, 10, 5, 3])[0]

        if home_team is None or away_team is None:
            continue

        # Generate events
        # INICIO_PRIMEIRO_TEMPO
        evento_id += 1
        evento_rows.append(
            f"  ({evento_id}, {pm_id}, 'INICIO_PRIMEIRO_TEMPO', 1, NULL, NULL, NULL, NULL, false)"
        )

        # Goals for home team
        home_players = [(home_team - 1) * PLAYERS_PER_TEAM + 1 + i for i in range(PLAYERS_PER_TEAM)]
        away_players = [(away_team - 1) * PLAYERS_PER_TEAM + 1 + i for i in range(PLAYERS_PER_TEAM)]

        for g in range(gols_m):
            evento_id += 1
            minuto = random.randint(3, 88)
            goleador = random.choice(home_players[4:])  # midfielders/forwards
            assistente = random.choices([random.choice(home_players), None], weights=[3, 1])[0]
            evento_rows.append(
                f"  ({evento_id}, {pm_id}, 'GOL', {minuto}, NULL, {home_team}, {goleador}, {assistente or 'NULL'}, false)"
            )

        for g in range(gols_v):
            evento_id += 1
            minuto = random.randint(3, 88)
            goleador = random.choice(away_players[4:])
            assistente = random.choices([random.choice(away_players), None], weights=[3, 1])[0]
            evento_rows.append(
                f"  ({evento_id}, {pm_id}, 'GOL', {minuto}, NULL, {away_team}, {goleador}, {assistente or 'NULL'}, false)"
            )

        # Yellow cards (0-3)
        for _ in range(random.randint(0, 3)):
            evento_id += 1
            minuto = random.randint(10, 89)
            time = random.choice([home_team, away_team])
            jogadores = home_players if time == home_team else away_players
            jogador = random.choice(jogadores[:8])  # defenders/midfielders
            evento_rows.append(
                f"  ({evento_id}, {pm_id}, 'CARTAO_AMARELO', {minuto}, NULL, {time}, {jogador}, NULL, false)"
            )

        # Red card (rare)
        if random.random() < 0.15:
            evento_id += 1
            minuto = random.randint(50, 89)
            time = random.choice([home_team, away_team])
            jogadores = home_players if time == home_team else away_players
            jogador = random.choice(jogadores[:10])
            evento_rows.append(
                f"  ({evento_id}, {pm_id}, 'CARTAO_VERMELHO', {minuto}, NULL, {time}, {jogador}, NULL, false)"
            )

        # Substitution (0-3)
        for _ in range(random.randint(0, 3)):
            evento_id += 1
            minuto = random.randint(55, 85)
            time = random.choice([home_team, away_team])
            jogadores = home_players if time == home_team else away_players
            jogador = random.choice(jogadores)
            evento_rows.append(
                f"  ({evento_id}, {pm_id}, 'SUBSTITUICAO', {minuto}, NULL, {time}, {jogador}, NULL, false)"
            )

        # FIM_PRIMEIRO_TEMPO
        evento_id += 1
        extra = random.choices([0, 1, 2, 3], weights=[40, 30, 20, 10])[0]
        evento_rows.append(
            f"  ({evento_id}, {pm_id}, 'FIM_PRIMEIRO_TEMPO', 45, {extra if extra > 0 else 'NULL'}, NULL, NULL, NULL, false)"
        )

        # INICIO_SEGUNDO_TEMPO
        evento_id += 1
        evento_rows.append(
            f"  ({evento_id}, {pm_id}, 'INICIO_SEGUNDO_TEMPO', 46, NULL, NULL, NULL, NULL, false)"
        )

        # FIM_PARTIDA
        evento_id += 1
        extra = random.choices([0, 1, 2, 3, 4, 5], weights=[20, 20, 30, 15, 10, 5])[0]
        evento_rows.append(
            f"  ({evento_id}, {pm_id}, 'FIM_PARTIDA', 90, {extra if extra > 0 else 'NULL'}, NULL, NULL, NULL, false)"
        )

    out(f"-- {len(event_match_ids)} partidas com eventos ({len(evento_rows)} eventos)")
    out("INSERT INTO public.eventos_partida (id, partida_id, tipo_evento, minuto, minuto_extra, time_id, jogador_id, jogador_secundario_id, anulado) VALUES")
    out(",\n".join(evento_rows) + ";")
    out("")

    # ---- ESCALAÇÕES ----
    out("-- ============================================================")
    out("-- 9. ESCALAÇÕES")
    out("-- ============================================================")
    escalacao_id = 0
    escalacao_rows = []

    for pm_id in event_match_ids:
        if pm_id <= 380:
            round_idx = (pm_id - 1) // 10
            match_in_round = (pm_id - 1) % 10
            matches_in_round = all_rounds[round_idx]
            if match_in_round < len(matches_in_round):
                home_team, away_team = matches_in_round[match_in_round]
            else:
                continue
        else:
            continue

        def make_lineup(team_id, team_player_offset, is_home):
            nonlocal escalacao_id
            rows = []
            base = (team_id - 1) * PLAYERS_PER_TEAM
            # 11 starters
            starters = list(range(base + 1, base + 12))
            # 4 reserves
            reserves = list(range(base + 12, base + 16))

            for p_id in starters:
                escalacao_id += 1
                pos_idx = p_id - base - 1
                pos = POSITIONS[pos_idx] if pos_idx < len(POSITIONS) else "MEIO_CAMPO"
                numero = (p_id % 99) + 1
                rows.append(
                    f"  ({escalacao_id}, {pm_id}, {p_id}, 'TITULAR', '{pos}', {numero}, true)"
                )
            for p_id in reserves:
                escalacao_id += 1
                pos_idx = p_id - base - 1
                pos = POSITIONS[pos_idx] if pos_idx < len(POSITIONS) else "MEIO_CAMPO"
                numero = (p_id % 99) + 1
                rows.append(
                    f"  ({escalacao_id}, {pm_id}, {p_id}, 'RESERVA', '{pos}', {numero}, true)"
                )
            return rows

        if home_team:
            escalacao_rows.extend(make_lineup(home_team, (home_team - 1) * PLAYERS_PER_TEAM, True))
        if away_team:
            escalacao_rows.extend(make_lineup(away_team, (away_team - 1) * PLAYERS_PER_TEAM, False))

    out(f"-- {len(event_match_ids)} partidas com escalações ({len(escalacao_rows)} entries)")
    out("INSERT INTO public.escalacao_partida (id, partida_id, jogador_id, funcao, posicao, numero_camisa, ativo) VALUES")
    out(",\n".join(escalacao_rows) + ";")
    out("")

    # ---- ESTATÍSTICAS DAS PARTIDAS ----
    out("-- ============================================================")
    out("-- 10. ESTATÍSTICAS DAS PARTIDAS")
    out("-- ============================================================")
    est_id = 0
    est_rows = []

    for pm_id in event_match_ids:
        if pm_id <= 380:
            round_idx = (pm_id - 1) // 10
            match_in_round = (pm_id - 1) % 10
            matches_in_round = all_rounds[round_idx]
            if match_in_round < len(matches_in_round):
                home_team, away_team = matches_in_round[match_in_round]
            else:
                continue
        else:
            continue

        est_id += 1
        posse_m = random.randint(38, 65)
        posse_v = 100 - posse_m
        finalizacoes_m = random.randint(5, 25)
        finalizacoes_v = random.randint(3, 20)
        fin_gol_m = random.randint(1, min(10, finalizacoes_m))
        fin_gol_v = random.randint(0, min(8, finalizacoes_v))
        faltas_m = random.randint(5, 22)
        faltas_v = random.randint(5, 22)
        esc_m = random.randint(1, 12)
        esc_v = random.randint(0, 10)
        ca_m = random.randint(0, 4)
        ca_v = random.randint(0, 4)
        cv_m = random.randint(0, 1)
        cv_v = random.randint(0, 1)
        def_m = random.randint(1, 8)
        def_v = random.randint(1, 8)

        est_rows.append(
            f"  ({est_id}, {pm_id}, {posse_m}, {posse_v}, {finalizacoes_m}, {finalizacoes_v}, {fin_gol_m}, {fin_gol_v}, {faltas_m}, {faltas_v}, {esc_m}, {esc_v}, {ca_m}, {ca_v}, {cv_m}, {cv_v}, {def_m}, {def_v})"
        )

    out(f"-- {len(est_rows)} partidas com estatísticas")
    out("INSERT INTO public.estatisticas_partida (id, partida_id,\n"
        "  posse_bola_mandante, posse_bola_visitante,\n"
        "  finalizacoes_mandante, finalizacoes_visitante,\n"
        "  finalizacoes_gol_mandante, finalizacoes_gol_visitante,\n"
        "  faltas_mandante, faltas_visitante,\n"
        "  escanteios_mandante, escanteios_visitante,\n"
        "  cartoes_amarelos_mandante, cartoes_amarelos_visitante,\n"
        "  cartoes_vermelhos_mandante, cartoes_vermelhos_visitante,\n"
        "  defesas_mandante, defesas_visitante)\n"
        "VALUES")
    out(",\n".join(est_rows) + ";")
    out("")

    # ---- ESTATÍSTICAS JOGADOR (iniciais) ----
    out("-- ============================================================")
    out("-- 11. ESTATÍSTICAS INICIAIS DOS JOGADORES")
    out("-- ============================================================")
    ej_rows = []
    for p_id in range(1, TOTAL_PLAYERS + 1):
        gols = random.choices([0, 0, 0, 1, 1, 2, 3, 5, 10], weights=[30, 20, 15, 10, 10, 5, 3, 2, 1])[0]
        assists = random.choices([0, 0, 1, 1, 2, 3, 5], weights=[30, 20, 15, 10, 10, 5, 3])[0]
        amarelos = random.choices([0, 0, 1, 1, 2, 3, 4, 5], weights=[20, 15, 20, 15, 10, 5, 3, 2])[0]
        vermelhos = random.choices([0, 0, 0, 0, 1, 1, 2], weights=[40, 20, 15, 10, 5, 3, 2])[0]
        finalizacoes = random.randint(gols * 3, gols * 8 + 5)
        faltas = random.randint(amarelos * 2, amarelos * 4 + 3)
        partidas = random.randint(5, 60)
        defesas = 0
        if p_id % POSITIONS.count("GOLEIRO") == 1:  # GK
            defesas = random.randint(20, 200)
        pen_def = random.randint(0, 3) if defesas > 0 else 0
        pen_perd = random.randint(0, 2)
        clean = random.randint(1, 30) if defesas > 0 else 0

        ej_rows.append(
            f"  ({p_id}, {gols}, {assists}, {finalizacoes}, {amarelos}, {vermelhos}, {faltas}, {partidas}, {defesas}, {pen_def}, {pen_perd}, {clean})"
        )

    out("INSERT INTO public.estatisticas_jogador (id, gols, assistencias, finalizacoes, cartoes_amarelos, cartoes_vermelhos, faltas_cometidas, partidas_jogadas, defesas, penaltis_defendidos, penaltis_perdidos, clean_sheets) VALUES")
    out(",\n".join(ej_rows) + ";")
    out("")

    # ---- ESTATÍSTICAS JOGADOR CAMPEONATO (para campeonato 1) ----
    out("-- ============================================================")
    out("-- 12. ESTATÍSTICAS JOGADOR NO CAMPEONATO 1")
    out("-- ============================================================")
    ejc_rows = []
    for p_id in range(1, TOTAL_PLAYERS + 1):
        gols = random.choices([0, 0, 0, 1, 1, 2, 3, 5, 7], weights=[30, 20, 15, 10, 10, 5, 3, 2, 1])[0]
        assists = random.choices([0, 0, 1, 1, 2, 3], weights=[30, 20, 15, 10, 10, 5])[0]
        amarelos = random.choices([0, 0, 1, 1, 2, 3], weights=[20, 15, 20, 15, 10, 5])[0]
        vermelhos = random.choices([0, 0, 0, 0, 1, 1], weights=[40, 20, 15, 10, 5, 3])[0]
        finalizacoes = random.randint(gols * 3, gols * 8 + 5)
        faltas = random.randint(amarelos * 2, amarelos * 4 + 3)
        partidas = random.randint(1, 30)
        defesas = 0
        if p_id % POSITIONS.count("GOLEIRO") == 1:  # GK
            defesas = random.randint(5, 80)
        clean = random.randint(0, 15) if defesas > 0 else 0

        ejc_rows.append(
            f"  ({p_id}, 0, {p_id}, 1, {gols}, {assists}, {finalizacoes}, {amarelos}, {vermelhos}, {faltas}, {partidas}, {defesas}, {clean})"
        )

    out("INSERT INTO public.estatisticas_jogador_campeonato (id, amarelos_desde_suspensao, jogador_id, campeonato_id, gols, assistencias, finalizacoes, cartoes_amarelos, cartoes_vermelhos, faltas_cometidas, partidas_jogadas, defesas, clean_sheets) VALUES")
    out(",\n".join(ejc_rows) + ";")
    out("")

    # ---- CAMPEONATO 2: Fases eliminatórias + confrontos (estrutura vazia) ----
    out("-- ============================================================")
    out("-- 13. FASES ELIMINATÓRIAS (Copa do Brasil)")
    out("-- ============================================================")
    out("INSERT INTO public.fases_eliminatorias (id, campeonato_id, fase, jogo_unico) VALUES")
    out("  (1, 2, 'OITAVAS', false),")
    out("  (2, 2, 'QUARTAS', false),")
    out("  (3, 2, 'SEMIFINAL', false),")
    out("  (4, 2, 'FINAL', true);")
    out("")

    # ---- Aviso ----
    out("-- ============================================================")
    out("-- PRONTO! Seed massivo gerado com sucesso.")
    out(f"-- {NUM_TEAMS} times | {TOTAL_PLAYERS} jogadores | {partida_id} partidas | {len(evento_rows)} eventos | {len(escalacao_rows)} escalações | {len(est_rows)} estatísticas")
    out("-- ============================================================")

    return "\n".join(lines)


if __name__ == "__main__":
    sql = generate_seed()
    output_path = "/home/guilherme/Documentos/StatZone/Projeto StatZone/statezone-frontend/seed_completo.sql"
    with open(output_path, "w") as f:
        f.write(sql)
    total_lines = sql.count("\n") + 1
    print(f"Seed gerado com sucesso: {output_path}")
    print(f"Total: {total_lines} linhas")
