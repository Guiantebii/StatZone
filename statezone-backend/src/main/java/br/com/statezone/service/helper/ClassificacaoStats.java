package br.com.statezone.service.helper;

import br.com.statezone.model.Time;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassificacaoStats {

    private Long timeId;
    private String nomeTime;

    private Integer pontos;
    private Integer jogos;

    private Integer vitorias;
    private Integer empates;
    private Integer derrotas;

    private Integer golsFeitos;
    private Integer golsSofridos;
    private Integer saldoGols;
    private Integer posicao;

    public ClassificacaoStats(Time time) {

        this.timeId = time.getId();
        this.nomeTime = time.getNome();

        this.pontos = 0;
        this.jogos = 0;

        this.vitorias = 0;
        this.empates = 0;
        this.derrotas = 0;

        this.golsFeitos = 0;
        this.golsSofridos = 0;
        this.saldoGols = 0;
        this.posicao = 0;
    }
}
