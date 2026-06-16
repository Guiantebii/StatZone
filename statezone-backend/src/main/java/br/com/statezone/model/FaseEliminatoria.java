package br.com.statezone.model;

import br.com.statezone.enums.FaseEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fases_eliminatorias")
public class FaseEliminatoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id", nullable = false)
    private Campeonato campeonato;

    @Enumerated(EnumType.STRING)
    @Column(name = "fase", nullable = false)
    private FaseEnum fase;

    @Column(name = "jogo_unico", nullable = false)
    private Boolean jogoUnico = false;

    @OneToMany(mappedBy = "faseEliminatoria", fetch = FetchType.LAZY)
    private List<ConfrontoEliminatorio> confrontos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}