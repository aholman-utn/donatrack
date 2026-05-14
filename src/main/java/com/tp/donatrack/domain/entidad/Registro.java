package com.tp.donatrack.domain.entidad;

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
