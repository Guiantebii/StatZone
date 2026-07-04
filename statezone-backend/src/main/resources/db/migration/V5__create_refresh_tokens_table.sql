-- Flyway migration: create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  token_id VARCHAR(255) NOT NULL UNIQUE,
  usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE,
  expiry_date TIMESTAMP WITH TIME ZONE,
  revoked BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_usuario_id ON refresh_tokens(usuario_id);
