package br.com.statezone.model;

import br.com.statezone.enums.Formacao;
import br.com.statezone.enums.StatusPartida;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "partidas")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String estadio;

    private String arbitro;

    private Integer rodada;

    @Column(name = "data_partida")
    private LocalDateTime dataPartida;

    @Enumerated(EnumType.STRING)
    private StatusPartida status;

    @Column(name = "gols_mandante", nullable = false)
    private Integer golsMandante = 0;

    @Column(name = "gols_visitante",nullable = false)
    private Integer golsVisitante = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_mandante_id")
    private Time timeMandante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_visitante_id")
    private Time timeVisitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id")
    private Campeonato campeonato;

    @Column(name = "gols_penaltis_mandante")
    private Integer golsPenaltisMandante;

    @Column(name = "gols_penaltis_visitante")
    private Integer golsPenaltisVisitante;

    @OneToMany(mappedBy = "partida", fetch = FetchType.LAZY)
    private List<EventoPartida> eventos;

    @OneToOne(mappedBy = "partida", fetch = FetchType.LAZY)
    private EstatisticasPartida estatisticas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @Column(name = "api_football_id", unique = true)
    private Long apiFootballId;

    @Enumerated(EnumType.STRING)
    @Column(name = "formacao_mandante")
    private Formacao formacaoMandante;

    @Enumerated(EnumType.STRING)
    @Column(name = "formacao_visitante")
    private Formacao formacaoVisitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fase_eliminatoria_id")
    private FaseEliminatoria faseEliminatoria;

    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}