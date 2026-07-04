ALTER TABLE public.partidas
    ADD COLUMN formacao_mandante character varying(20),
    ADD COLUMN formacao_visitante character varying(20);

ALTER TABLE public.partidas
    ADD CONSTRAINT partidas_formacao_mandante_check
        CHECK (((formacao_mandante)::text = ANY (
            (ARRAY['_4_4_2'::character varying,
                   '_4_3_3'::character varying,
                   '_4_2_3_1'::character varying,
                   '_3_5_2'::character varying,
                   '_3_4_3'::character varying,
                   '_4_1_4_1'::character varying,
                   '_5_3_2'::character varying,
                   '_4_4_1_1'::character varying])::text[])));

ALTER TABLE public.partidas
    ADD CONSTRAINT partidas_formacao_visitante_check
        CHECK (((formacao_visitante)::text = ANY (
            (ARRAY['_4_4_2'::character varying,
                   '_4_3_3'::character varying,
                   '_4_2_3_1'::character varying,
                   '_3_5_2'::character varying,
                   '_3_4_3'::character varying,
                   '_4_1_4_1'::character varying,
                   '_5_3_2'::character varying,
                   '_4_4_1_1'::character varying])::text[])));
