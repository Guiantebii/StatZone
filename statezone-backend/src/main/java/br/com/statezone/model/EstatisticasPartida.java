package br.com.statezone.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estatisticas_partida")
public class EstatisticasPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "posse_bola_mandante")
    private Integer posseBolaMandante = 0;

    @Column(name = "posse_bola_visitante")
    private Integer posseBolaVisitante = 0;

    @Column(name = "finalizacoes_mandante")
    private Integer finalizacoesMandante = 0;

    @Column(name = "finalizacoes_visitante")
    private Integer finalizacoesVisitante = 0;

    @Column(name = "finalizacoes_gol_mandante")
    private Integer finalizacoesGolMandante = 0;

    @Column(name = "finalizacoes_gol_visitante")
    private Integer finalizacoesGolVisitante = 0;

    @Column(name = "faltas_mandante")
    private Integer faltasMandante = 0;

    @Column(name = "faltas_visitante")
    private Integer faltasVisitante = 0;

    @Column(name = "escanteios_mandante")
    private Integer escanteiosMandante = 0;

    @Column(name = "escanteios_visitante")
    private Integer escanteiosVisitante = 0;

    @Column(name = "cartoes_amarelos_mandante")
    private Integer cartoesAmarelosMandante = 0;

    @Column(name = "cartoes_amarelos_visitante")
    private Integer cartoesAmarelosVisitante = 0;

    @Column(name = "cartoes_vermelhos_mandante")
    private Integer cartoesVermelhosMandante = 0;

    @Column(name = "cartoes_vermelhos_visitante")
    private Integer cartoesVermelhosVisitante = 0;

    @Column(name = "defesas_mandante")
    private Integer defesasMandante = 0;

    @Column(name = "defesas_visitante")
    private Integer defesasVisitante = 0;

    @Column(name = "penaltis_defendidos_mandante")
    private Integer penaltisDefendidosMandante = 0;

    @Column(name = "penaltis_defendidos_visitante")
    private Integer penaltisDefendidosVisitante = 0;

    @Column(name = "substituicoes_mandante")
    private Integer substituicoesMandante = 0;

    @Column(name = "substituicoes_visitante")
    private Integer substituicoesVisitante = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private Partida partida;
}