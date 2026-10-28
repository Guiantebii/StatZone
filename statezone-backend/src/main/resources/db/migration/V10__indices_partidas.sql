CREATE INDEX IF NOT EXISTS idx_partidas_status_data ON partidas (status, data_partida);
CREATE INDEX IF NOT EXISTS idx_partidas_campeonato_status ON partidas (campeonato_id, status);
