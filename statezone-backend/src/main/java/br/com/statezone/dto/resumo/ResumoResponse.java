package br.com.statezone.dto.resumo;

public record ResumoResponse(
        long campeonatos,
        long times,
        long jogadores,
        long partidas,
        Long primeiroCampeonatoId
) {
}
