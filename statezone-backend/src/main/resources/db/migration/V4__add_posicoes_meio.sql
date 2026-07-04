ALTER TABLE public.jogadores DROP CONSTRAINT IF EXISTS jogadores_posicao_check;
ALTER TABLE public.jogadores ADD CONSTRAINT jogadores_posicao_check CHECK (
    (posicao)::text = ANY (
        (ARRAY['GOLEIRO'::character varying,
               'ZAGUEIRO'::character varying,
               'LATERAL_DIREITO'::character varying,
               'LATERAL_ESQUERDO'::character varying,
               'VOLANTE'::character varying,
               'MEIO_CAMPO'::character varying,
               'MEIO_ESQUERDO'::character varying,
               'MEIO_DIREITO'::character varying,
               'PONTA_DIREITA'::character varying,
               'PONTA_ESQUERDA'::character varying,
               'MEIA_ATACANTE'::character varying,
               'CENTROAVANTE'::character varying])::text[]
    )
);

ALTER TABLE public.escalacao_partida DROP CONSTRAINT IF EXISTS escalacao_partida_posicao_check;
ALTER TABLE public.escalacao_partida ADD CONSTRAINT escalacao_partida_posicao_check CHECK (
    (posicao)::text = ANY (
        (ARRAY['GOLEIRO'::character varying,
               'ZAGUEIRO'::character varying,
               'LATERAL_DIREITO'::character varying,
               'LATERAL_ESQUERDO'::character varying,
               'VOLANTE'::character varying,
               'MEIO_CAMPO'::character varying,
               'MEIO_ESQUERDO'::character varying,
               'MEIO_DIREITO'::character varying,
               'PONTA_DIREITA'::character varying,
               'PONTA_ESQUERDA'::character varying,
               'MEIA_ATACANTE'::character varying,
               'CENTROAVANTE'::character varying])::text[]
    )
);
