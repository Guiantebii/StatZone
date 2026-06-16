package br.com.statezone.model;

import br.com.statezone.enums.StatusConfronto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "confrontos_eliminatorios")
public class ConfrontoEliminatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fase_eliminatoria_id")
    private FaseEliminatoria faseEliminatoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_a_id")
    private Time timeA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_b_id")
    private Time timeB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_classificado_id")
    private Time timeClassificado;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_ida_id")
    private Partida partidaIda;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_volta_id")
    private Partida partidaVolta;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proximo_confronto_id")
    private ConfrontoEliminatorio proximoConfronto;

    private Integer slotProximo;

    private Integer bracketIndex;
    private Integer roundIndex;
    private Integer seed;


    @Enumerated(EnumType.STRING)
    private StatusConfronto statusConfronto = StatusConfronto.PENDENTE;

    private Boolean jogoUnico = false;

    @Column(name = "gols_penaltis_a")
    private Integer golsPenaltisA;

    @Column(name = "gols_penaltis_b")
    private Integer golsPenaltisB;

    @CreationTimestamp
    private LocalDateTime criadoEm;
}