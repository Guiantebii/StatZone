package br.com.statezone.model;

import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.Posicao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "escalacao_partida",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"partida_id", "jogador_id"}
        ))
public class EscalacaoPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogador_id", nullable = false)
    private Jogador jogador;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcao", nullable = false)
    private FuncaoEscalacao funcao;

    @Enumerated(EnumType.STRING)
    @Column(name = "posicao")
    private Posicao posicao;

    @Column(name = "numero_camisa")
    private Integer numeroCamisa;



    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}