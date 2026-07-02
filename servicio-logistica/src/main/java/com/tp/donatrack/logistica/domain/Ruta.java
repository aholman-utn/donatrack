package com.tp.donatrack.logistica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {
    private Long id;
    //private Long camionId;
    private Camion camion;
    //private Long choferId;
    private Chofer chofer;
    private List<Parada> paradas;
    private Boolean iniciada;

    public void iniciarRuta() {
        if (Boolean.TRUE.equals(this.iniciada)) {
            throw new IllegalStateException("La ruta ya se encuentra iniciada");
        }
        this.iniciada = true;
    }
}
