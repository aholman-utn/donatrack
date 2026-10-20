package com.tp.donatrack.logistica.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "rutas")
public class Ruta {
    @Id
    private String id = null;

    private Camion camion;

    private Chofer chofer;

    @Builder.Default
    private List<Parada> paradas = new ArrayList<>();

    @Builder.Default
    private Boolean iniciada = false;

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
