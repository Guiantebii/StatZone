package br.com.statezone.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    private Integer posseBolaMandante;

    @Column(name = "posse_bola_visitante")
    private Integer posseBolaVisitante;

    @Column(name = "finalizacoes_mandante")
    private Integer finalizacoesMandante;

    @Column(name = "finalizacoes_visitante")
    private Integer finalizacoesVisitante;

    @Column(name = "finalizacoes_gol_mandante")
    private Integer finalizacoesGolMandante;

    @Column(name = "finalizacoes_gol_visitante")
    private Integer finalizacoesGolVisitante;

    @Column(name = "faltas_mandante")
    private Integer faltasMandante;

    @Column(name = "faltas_visitante")
    private Integer faltasVisitante;

    @Column(name = "escanteios_mandante")
    private Integer escanteiosMandante;

    @Column(name = "escanteios_visitante")
    private Integer escanteiosVisitante;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private Partida partida;

    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}