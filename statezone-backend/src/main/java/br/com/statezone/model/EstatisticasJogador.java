package br.com.statezone.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estatisticas_jogador")
public class EstatisticasJogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogador_id")
    private Jogador jogador;

    @Column(name = "gols")
    private Integer gols = 0;

    @Column(name = "assistencias")
    private Integer assistencias = 0;

    @Column(name = "finalizacoes")
    private Integer finalizacoes = 0;

    @Column(name = "cartoes_amarelos")
    private Integer cartoesAmarelos = 0;

    @Column(name = "cartoes_vermelhos")
    private Integer cartoesVermelhos = 0;

    @Column(name = "faltas_cometidas")
    private Integer faltasCometidas = 0;

    @Column(name = "partidas_jogadas")
    private Integer partidasJogadas = 0;
}