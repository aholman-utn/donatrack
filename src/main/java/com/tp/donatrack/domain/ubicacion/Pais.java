package com.tp.donatrack.domain.ubicacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pais {
    private String nombre;

    public Pais(String paisNombre) {
        this.nombre = paisNombre;
    }
}
