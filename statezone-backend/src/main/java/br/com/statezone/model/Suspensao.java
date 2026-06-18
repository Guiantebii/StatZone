package br.com.statezone.model;

import br.com.statezone.enums.MotivoSuspensao;
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
@Table(name = "suspensoes")
public class Suspensao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogador_id", nullable = false)
    private Jogador jogador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id", nullable = false)
    private Campeonato campeonato;

    @Column(name = "rodada_suspensao")
    private Integer rodadaSuspensao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_alvo_id")
    private Partida partidaAlvo;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false)
    private MotivoSuspensao motivo;

    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}
