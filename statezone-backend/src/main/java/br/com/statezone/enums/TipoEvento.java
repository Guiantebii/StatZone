package br.com.statezone.enums;

public enum TipoEvento {

    // GOLS
    GOL(true, true, false, true),
    GOL_CONTRA(true, true, false, true),
    PENALTI_GOL(true, true, false, true),
    PENALTI_PERDIDO(false, false, false, true),

    // FINALIZAÇÃO
    FINALIZACAO(false, true, false, true),

    // DISCIPLINA
    FALTA(false, false, true, false),
    CARTAO_AMARELO(false, false, true, false),
    CARTAO_VERMELHO(false, false, true, false),
    IMPEDIMENTO(false, false, false, true),

    // TÁTICO
    ESCANTEIO(false, false, false, true),
    SUBSTITUICAO(false, false, false, false),

    // SUPORTE
    ASSISTENCIA(false, true, false, true);

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