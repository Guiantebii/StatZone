package br.com.statezone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estatisticas_jogador_campeonato",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"jogador_id", "campeonato_id"}
        ))
public class EstatisticasJogadorCampeonato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogador_id", nullable = false)
    private Jogador jogador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id", nullable = false)
    private Campeonato campeonato;

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

    @Column(name = "defesas")
    private Integer defesas = 0;

    @Column(name = "penaltis_defendidos")
    private Integer penaltisDefendidos = 0;

    @Column(name = "penaltis_perdidos")
    private Integer penaltisPerdidos = 0;

    @Column(name = "clean_sheets")
    private Integer cleanSheets = 0;
}