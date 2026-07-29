ALTER TABLE campeonatos ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'RASCUNHO';

ALTER TABLE campeonatos
    ADD CONSTRAINT campeonatos_status_check CHECK (
        status IN ('RASCUNHO', 'ATIVO')
    );
