package br.com.statezone.util;

public final class Paginacao {

    public static final int MAX_TAMANHO = 50;

    private Paginacao() {
    }

    public static int pagina(int pagina) {
        return Math.max(pagina, 0);
    }

    public static int tamanho(int tamanho) {
        if (tamanho < 1) {
            return 1;
        }
        return Math.min(tamanho, MAX_TAMANHO);
    }
}
