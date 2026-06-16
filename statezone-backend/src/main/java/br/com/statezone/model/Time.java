package br.com.statezone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "times")
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String sigla;

    private String cidade;

    private String pais;

    @Column(name = "escudo_url")
    private String escudoUrl;

    private String tecnico;

    private String estadio;

    @Column(name = "seed")
    private Integer seed;

    @Column(name = "fundado_em")
    private LocalDate fundadoEm;

    @OneToMany(mappedBy = "time", fetch = FetchType.LAZY)
    private List<Jogador> jogadores;

    @OneToMany(mappedBy = "timeMandante", fetch = FetchType.LAZY)
    private List<Partida> partidasMandante;

    @OneToMany(mappedBy = "timeVisitante", fetch = FetchType.LAZY)
    private List<Partida> partidasVisitante;


    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}