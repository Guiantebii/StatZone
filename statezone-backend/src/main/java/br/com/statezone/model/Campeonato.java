package br.com.statezone.model;

import br.com.statezone.enums.TipoFormato;
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
@Table(name = "campeonatos")
public class Campeonato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String pais;

    private String temporada;

    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column(name = "amarelos_para_suspensao")
    private Integer amarelosParaSuspensao = 3;

    @OneToMany(mappedBy = "campeonato", fetch = FetchType.LAZY)
    private List<Partida> partidas;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_formato")
    private TipoFormato tipoFormato = TipoFormato.PONTOS_CORRIDOS;

    @ManyToMany
    @JoinTable(
            name = "campeonato_times",
            joinColumns = @JoinColumn(name = "campeonato_id"),
            inverseJoinColumns = @JoinColumn(name = "time_id")
    )
    private List<Time> times;

    @CreationTimestamp
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}