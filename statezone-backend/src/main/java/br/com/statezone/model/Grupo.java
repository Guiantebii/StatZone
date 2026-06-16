package br.com.statezone.model;

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
@Table(name = "grupos",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"campeonato_id", "nome"}
        ))
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id", nullable = false)
    private Campeonato campeonato;

    @Column(name = "nome", nullable = false)
    private String nome;

    @ManyToMany
    @JoinTable(
            name = "grupo_times",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "time_id")
    )
    private List<Time> times = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}