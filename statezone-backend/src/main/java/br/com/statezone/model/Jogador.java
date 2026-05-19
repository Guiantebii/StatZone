package br.com.statezone.model;

import br.com.statezone.enums.PeForte;
import br.com.statezone.enums.Posicao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jogadores")
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String nacionalidade;

    @Enumerated(EnumType.STRING)
    private Posicao posicao;

    @Column(name = "numero_camisa")
    private Integer numeroCamisa;

    private BigDecimal altura;

    private BigDecimal peso;

    @Column(name = "valor_mercado")
    private BigDecimal valorMercado;

    @Enumerated(EnumType.STRING)
    @Column(name = "pe_forte")
    private PeForte peForte;

    @Column(name = "foto_url")
    private String fotoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private Time time;

    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}