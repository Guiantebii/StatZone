ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_role_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_role_check CHECK (role::text = ANY (ARRAY['USER'::character varying, 'ADMIN'::character varying, 'OPERADOR'::character varying]::text[]));
