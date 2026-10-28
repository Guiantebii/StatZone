CREATE TABLE rate_limit_tentativas (
    id BIGSERIAL PRIMARY KEY,
    chave VARCHAR(128) NOT NULL,
    instante TIMESTAMP NOT NULL
);

CREATE INDEX idx_rate_limit_chave_instante ON rate_limit_tentativas (chave, instante);
