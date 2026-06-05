package br.com.statezone.enums;

public enum TipoEvento {

    // GOLS
    GOL(true, true, false, true),
    GOL_CONTRA(true, true, false, true),
    PENALTI_GOL(true, true, false, true),
    PENALTI_PERDIDO(false, false, false, true),

    // CRIAÇÃO DE JOGADAS
    ASSISTENCIA(false, true, false, true),

    // FINALIZAÇÕES
    FINALIZACAO(false, true, false, true),
    FINALIZACAO_NO_GOL(false, true, false, true),

    // GOLEIRO
    DEFESA(false, false, false, false),
    PENALTI_DEFENDIDO(false, false, false, false),

    // DISCIPLINA
    FALTA(false, false, true, false),
    CARTAO_AMARELO(false, false, true, false),
    CARTAO_VERMELHO(false, false, true, false),

    // OFENSIVOS
    IMPEDIMENTO(false, false, false, true),
    ESCANTEIO(false, false, false, true),

    // TÁTICOS
    SUBSTITUICAO_ENTRADA(false, false, false, false),
    SUBSTITUICAO_SAIDA(false, false, false, false);

    private final boolean geraGol;
    private final boolean contaFinalizacao;
    private final boolean disciplina;
    private final boolean ofensivo;

    TipoEvento(
            boolean geraGol,
            boolean contaFinalizacao,
            boolean disciplina,
            boolean ofensivo
    ) {
        this.geraGol = geraGol;
        this.contaFinalizacao = contaFinalizacao;
        this.disciplina = disciplina;
        this.ofensivo = ofensivo;
    }

    public boolean isGeraGol() {
        return geraGol;
    }

    public boolean isContaFinalizacao() {
        return contaFinalizacao;
    }

    public boolean isDisciplina() {
        return disciplina;
    }

    public boolean isOfensivo() {
        return ofensivo;
    }
}