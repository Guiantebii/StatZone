-- ============================================================
-- SEED PARA TESTE — StateZone
-- Popula o banco com dados realistas
-- ============================================================
-- ANTES DE RODAR: gere um BCrypt hash para a senha do admin:
--   https://bcrypt-generator.com → "admin123" → rounds=10
--   Substitua o hash abaixo pelo gerado.
-- ============================================================

-- Limpa dados existentes (ordem correta respeitando FKs)
TRUNCATE TABLE public.suspensoes CASCADE;
TRUNCATE TABLE public.processamento_confronto_pendente CASCADE;
TRUNCATE TABLE public.estatisticas_jogador_campeonato CASCADE;
TRUNCATE TABLE public.estatisticas_jogador CASCADE;
TRUNCATE TABLE public.estatisticas_partida CASCADE;
TRUNCATE TABLE public.eventos_partida CASCADE;
TRUNCATE TABLE public.escalacao_partida CASCADE;
TRUNCATE TABLE public.confrontos_eliminatorios CASCADE;
TRUNCATE TABLE public.fases_eliminatorias CASCADE;
TRUNCATE TABLE public.grupo_times CASCADE;
TRUNCATE TABLE public.grupos CASCADE;
TRUNCATE TABLE public.partidas CASCADE;
TRUNCATE TABLE public.campeonato_times CASCADE;
TRUNCATE TABLE public.jogadores CASCADE;
TRUNCATE TABLE public.times CASCADE;
TRUNCATE TABLE public.campeonatos CASCADE;
TRUNCATE TABLE public.usuarios CASCADE;

-- Reseta sequences
SELECT setval('public.usuarios_id_seq', 1, false);
SELECT setval('public.campeonatos_id_seq', 1, false);
SELECT setval('public.times_id_seq', 1, false);
SELECT setval('public.jogadores_id_seq', 1, false);
SELECT setval('public.partidas_id_seq', 1, false);
SELECT setval('public.eventos_partida_id_seq', 1, false);
SELECT setval('public.escalacao_partida_id_seq', 1, false);
SELECT setval('public.estatisticas_partida_id_seq', 1, false);

-- ============================================================
-- 1. ADMIN
-- ============================================================
INSERT INTO public.usuarios (id, email, role, senha) VALUES
  (1, 'admin@statezone.com', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- ============================================================
-- 2. TIMES
-- ============================================================
INSERT INTO public.times (id, nome, sigla, pais, cidade, estadio, escudo_url) VALUES
  (1, 'Flamengo',     'FLA', 'Brasil', 'Rio de Janeiro',    'Maracanã',              NULL),
  (2, 'Palmeiras',    'PAL', 'Brasil', 'São Paulo',         'Allianz Parque',        NULL),
  (3, 'São Paulo',    'SAO', 'Brasil', 'São Paulo',         'Morumbis',              NULL),
  (4, 'Corinthians',  'COR', 'Brasil', 'São Paulo',         'Neo Química Arena',     NULL),
  (5, 'Grêmio',       'GRE', 'Brasil', 'Porto Alegre',      'Arena do Grêmio',       NULL),
  (6, 'Internacional','INT', 'Brasil', 'Porto Alegre',      'Beira-Rio',             NULL),
  (7, 'Santos',       'SAN', 'Brasil', 'Santos',            'Vila Belmiro',          NULL),
  (8, 'Cruzeiro',     'CRU', 'Brasil', 'Belo Horizonte',    'Mineirão',              NULL);

-- ============================================================
-- 3. CAMPEONATO
-- ============================================================
INSERT INTO public.campeonatos (id, nome, pais, temporada, tipo_formato, logo_url, amarelos_para_suspensao) VALUES
  (1, 'Brasileirão Série A 2025', 'Brasil', '2025', 'PONTOS_CORRIDOS', NULL, 3);

INSERT INTO public.campeonato_times (campeonato_id, time_id) VALUES
  (1,1), (1,2), (1,3), (1,4), (1,5), (1,6), (1,7), (1,8);

-- ============================================================
-- 4. JOGADORES (4 por time = 32)
-- ============================================================
INSERT INTO public.jogadores (id, nome, posicao, numero_camisa, time_id, nacionalidade) VALUES
  -- Flamengo
  (1,  'Gabriel Barbosa',       'CENTROAVANTE',    9,  1, 'Brasil'),
  (2,  'Giorgian De Arrascaeta','MEIA_ATACANTE',   14, 1, 'Uruguai'),
  (3,  'Gerson',                'MEIO_CAMPO',      8,  1, 'Brasil'),
  (4,  'Pedro',                 'CENTROAVANTE',    21, 1, 'Brasil'),
  -- Palmeiras
  (5,  'Raphael Veiga',         'MEIA_ATACANTE',   23, 2, 'Brasil'),
  (6,  'Dudu',                  'PONTA_ESQUERDA',  7,  2, 'Brasil'),
  (7,  'Gustavo Gómez',         'ZAGUEIRO',        15, 2, 'Paraguai'),
  (8,  'Weverton',              'GOLEIRO',         21, 2, 'Brasil'),
  -- São Paulo
  (9,  'Calleri',               'CENTROAVANTE',    9,  3, 'Argentina'),
  (10, 'Lucas Moura',           'PONTA_DIREITA',   7,  3, 'Brasil'),
  (11, 'Alisson',               'MEIO_CAMPO',      8,  3, 'Brasil'),
  (12, 'Rafael',                'GOLEIRO',         23, 3, 'Brasil'),
  -- Corinthians
  (13, 'Yuri Alberto',          'CENTROAVANTE',    9,  4, 'Brasil'),
  (14, 'Rodrigo Garro',         'MEIA_ATACANTE',   10, 4, 'Argentina'),
  (15, 'Fagner',                'LATERAL_DIREITO', 23, 4, 'Brasil'),
  (16, 'Cássio',                'GOLEIRO',         12, 4, 'Brasil'),
  -- Grêmio
  (17, 'Luis Suárez',           'CENTROAVANTE',    9,  5, 'Uruguai'),
  (18, 'Ferreira',              'PONTA_ESQUERDA',  7,  5, 'Brasil'),
  (19, 'Villassanti',           'VOLANTE',         8,  5, 'Paraguai'),
  (20, 'Marcos Felipe',         'GOLEIRO',         1,  5, 'Brasil'),
  -- Internacional
  (21, 'Alan Patrick',          'MEIA_ATACANTE',   10, 6, 'Brasil'),
  (22, 'Wanderson',             'PONTA_DIREITA',   11, 6, 'Brasil'),
  (23, 'Gabriel Mercado',       'ZAGUEIRO',        25, 6, 'Argentina'),
  (24, 'Sergio Rochet',         'GOLEIRO',         1,  6, 'Uruguai'),
  -- Santos
  (25, 'Marcos Leonardo',       'CENTROAVANTE',    9,  7, 'Brasil'),
  (26, 'Soteldo',               'PONTA_ESQUERDA',  10, 7, 'Venezuela'),
  (27, 'João Paulo',            'GOLEIRO',         1,  7, 'Brasil'),
  (28, 'Lucas Lima',            'MEIO_CAMPO',      20, 7, 'Brasil'),
  -- Cruzeiro
  (29, 'Gilberto',              'CENTROAVANTE',    9,  8, 'Brasil'),
  (30, 'Matheus Pereira',       'MEIA_ATACANTE',   10, 8, 'Brasil'),
  (31, 'Lucas Romero',          'VOLANTE',         8,  8, 'Argentina'),
  (32, 'Rafael Cabral',         'GOLEIRO',         1,  8, 'Brasil');

-- ============================================================
-- 5. PARTIDAS
-- ============================================================
INSERT INTO public.partidas (id, campeonato_id, time_mandante_id, time_visitante_id, data_partida, estadio, arbitro, rodada, status, gols_mandante, gols_visitante) VALUES
  (1, 1, 1, 2, '2025-04-12 16:00:00', 'Maracanã',           'Anderson Daronco',   1, 'ENCERRADA', 2, 1),
  (2, 1, 3, 4, '2025-04-12 18:30:00', 'Morumbis',           'Wilton Sampaio',    1, 'ENCERRADA', 1, 1),
  (3, 1, 5, 6, '2025-04-13 16:00:00', 'Arena do Grêmio',    'Raphael Claus',     1, 'AO_VIVO',   1, 0),
  (4, 1, 7, 8, '2025-04-13 18:30:00', 'Vila Belmiro',       'Bráulio Machado',   1, 'AO_VIVO',   0, 0),
  (5, 1, 2, 3, '2025-04-19 16:00:00', 'Allianz Parque',     'Ramon Abatti',      2, 'AGENDADA',  0, 0),
  (6, 1, 4, 1, '2025-04-19 18:30:00', 'Neo Química Arena',  'Rafael Klein',      2, 'AGENDADA',  0, 0),
  (7, 1, 6, 8, '2025-04-20 16:00:00', 'Beira-Rio',          'Savio Pereira',     2, 'AGENDADA',  0, 0);

-- ============================================================
-- 6. EVENTOS DAS PARTIDAS
-- ============================================================
-- Partida 1: Flamengo 2 x 1 Palmeiras (ENCERRADA)
INSERT INTO public.eventos_partida (id, partida_id, tipo_evento, minuto, minuto_extra, time_id, jogador_id, jogador_secundario_id, anulado) VALUES
  (1,  1, 'INICIO_PRIMEIRO_TEMPO',  1,  NULL, NULL, NULL, NULL, false),
  (2,  1, 'GOL',                   22,  NULL, 1,    1,    2,    false),
  (3,  1, 'CARTAO_AMARELO',        34,  NULL, 2,    7,    NULL, false),
  (4,  1, 'FIM_PRIMEIRO_TEMPO',    45,  2,    NULL, NULL, NULL, false),
  (5,  1, 'INICIO_SEGUNDO_TEMPO',  46,  NULL, NULL, NULL, NULL, false),
  (6,  1, 'GOL',                   55,  NULL, 2,    5,    6,    false),
  (7,  1, 'GOL',                   78,  NULL, 1,    4,    2,    false),
  (8,  1, 'CARTAO_VERMELHO',       82,  NULL, 2,    7,    NULL, false),
  (9,  1, 'SUBSTITUICAO',          85,  NULL, 1,    NULL, NULL, false),
  (10, 1, 'FIM_PARTIDA',           90,  3,    NULL, NULL, NULL, false);

-- Partida 2: São Paulo 1 x 1 Corinthians (ENCERRADA)
INSERT INTO public.eventos_partida (id, partida_id, tipo_evento, minuto, time_id, jogador_id, anulado) VALUES
  (11, 2, 'INICIO_PRIMEIRO_TEMPO',  1,  NULL, NULL, false),
  (12, 2, 'GOL',                   15, 3,    9,    false),
  (13, 2, 'CARTAO_AMARELO',        28, 4,    15,   false),
  (14, 2, 'FIM_PRIMEIRO_TEMPO',    45, NULL, NULL, false),
  (15, 2, 'INICIO_SEGUNDO_TEMPO',  46, NULL, NULL, false),
  (16, 2, 'GOL',                   62, 4,    13,   false),
  (17, 2, 'CARTAO_AMARELO',        75, 3,    11,   false),
  (18, 2, 'FIM_PARTIDA',           90, NULL, NULL, false);

-- Partida 3: Grêmio 1 x 0 Internacional (AO_VIVO)
INSERT INTO public.eventos_partida (id, partida_id, tipo_evento, minuto, time_id, jogador_id, anulado) VALUES
  (19, 3, 'INICIO_PRIMEIRO_TEMPO',  1,  NULL, NULL, false),
  (20, 3, 'CARTAO_AMARELO',        12, 6,    23,   false),
  (21, 3, 'GOL',                   38, 5,    17,   false),
  (22, 3, 'FIM_PRIMEIRO_TEMPO',    45, NULL, NULL, false);

-- ============================================================
-- 7. ESCALAÇÕES (Partida 1)
-- ============================================================
INSERT INTO public.escalacao_partida (id, partida_id, jogador_id, funcao, posicao, numero_camisa, ativo) VALUES
  -- Flamengo titulares
  (1, 1, 1, 'TITULAR', 'CENTROAVANTE',    9,  true),
  (2, 1, 2, 'TITULAR', 'MEIA_ATACANTE',   14, true),
  (3, 1, 3, 'TITULAR', 'MEIO_CAMPO',      8,  true),
  (4, 1, 4, 'TITULAR', 'CENTROAVANTE',    21, true),
  -- Palmeiras titulares
  (5, 1, 5, 'TITULAR', 'MEIA_ATACANTE',   23, true),
  (6, 1, 6, 'TITULAR', 'PONTA_ESQUERDA',  7,  true),
  (7, 1, 7, 'TITULAR', 'ZAGUEIRO',        15, true),
  (8, 1, 8, 'TITULAR', 'GOLEIRO',         21, true);

-- ============================================================
-- 8. ESTATÍSTICAS DAS PARTIDAS
-- ============================================================
INSERT INTO public.estatisticas_partida (id, partida_id,
  posse_bola_mandante, posse_bola_visitante,
  finalizacoes_mandante, finalizacoes_visitante,
  finalizacoes_gol_mandante, finalizacoes_gol_visitante,
  faltas_mandante, faltas_visitante,
  escanteios_mandante, escanteios_visitante,
  cartoes_amarelos_mandante, cartoes_amarelos_visitante,
  cartoes_vermelhos_mandante, cartoes_vermelhos_visitante,
  defesas_mandante, defesas_visitante)
VALUES
  (1, 1, 58, 42, 14, 8, 6, 3, 12, 15, 7, 4, 1, 2, 0, 1, 2, 5),
  (2, 2, 52, 48, 10, 11, 4, 5, 14, 13, 5, 6, 2, 1, 0, 0, 4, 3),
  (3, 3, 55, 45, 9,  6,  3, 1, 10, 12, 4, 3, 1, 1, 0, 0, 1, 2);
