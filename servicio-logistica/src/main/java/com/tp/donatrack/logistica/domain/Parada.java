package com.tp.donatrack.logistica.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "paradas")
public class Parada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id")
    @JsonIgnore
    private Ruta ruta;

    private Integer orden;
    private String direccion;

    @OneToMany(mappedBy = "parada", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Envio> envios = new ArrayList<>();

    @Transient
    private List<Long> enviosIds;

    public List<Long> getEnviosIds() {
        if (envios != null && !envios.isEmpty()) {
            return envios.stream().map(Envio::getId).toList();
        }
        return enviosIds != null ? enviosIds : new ArrayList<>();
    }

    public void setEnviosIds(List<Long> enviosIds) {
        this.enviosIds = enviosIds;
    }

    public void agregarEnvio(Envio envio) {
        if (this.envios == null) {
            this.envios = new ArrayList<>();
        }
        this.envios.add(envio);
        envio.setParada(this);
    }
}
