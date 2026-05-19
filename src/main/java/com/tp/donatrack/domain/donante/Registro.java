package com.tp.donatrack.domain.donante;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Registro {

    private String descripcion;

    public Registro(String descripcion) {
        this.descripcion = descripcion;
    }
}
