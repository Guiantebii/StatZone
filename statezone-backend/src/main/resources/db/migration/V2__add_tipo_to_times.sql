ALTER TABLE public.times ADD COLUMN tipo character varying(10) NOT NULL DEFAULT 'CLUBE';
ALTER TABLE public.times ADD CONSTRAINT times_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CLUBE'::character varying, 'SELECAO'::character varying])::text[])));
