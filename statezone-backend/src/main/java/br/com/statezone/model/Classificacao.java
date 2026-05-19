package br.com.statezone.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classificacoes")
public class Classificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partidas_jogadas")
    private Integer partidasJogadas;

    private Integer pontos;

    private Integer vitorias;

    private Integer empates;

    private Integer derrotas;

    @Column(name = "gols_feitos")
    private Integer golsFeitos;

    @Column(name = "gols_sofridos")
    private Integer golsSofridos;

    @Column(name = "saldo_gols")
    private Integer saldoGols;

    private Integer posicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id")
    private Campeonato campeonato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private Time time;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}