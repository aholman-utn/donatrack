package com.tp.donatrack.domain.bien;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class BienDuradero extends Bien {
    private EstadoBien estado;

    public BienDuradero(
        String nombre,
        String descripcion,
        String foto,
        SubCategoria subCategoria,
        EstadoBien estado
    ) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = foto;
        this.subCategoria = subCategoria;
        this.estado = estado;
    }

    @Override
    public Object getCriterioSegmentacion() {
        return this.estado; 
    }
}