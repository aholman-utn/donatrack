package com.tp.donatrack.logistica.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rutas")
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "camion_id")
    private Camion camion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chofer_id")
    private Chofer chofer;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Parada> paradas = new ArrayList<>();

    @Builder.Default
    private Boolean iniciada = false;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public void iniciarRuta() {
        if (Boolean.TRUE.equals(this.iniciada)) {
            throw new IllegalStateException("La ruta ya se encuentra iniciada");
        }
        this.iniciada = true;
    }

    public void agregarParada(Parada parada) {
        if (this.paradas == null) {
            this.paradas = new ArrayList<>();
        }
        this.paradas.add(parada);
        parada.setRuta(this);
    }
}
