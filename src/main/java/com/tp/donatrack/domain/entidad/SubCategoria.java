package com.tp.donatrack.domain.entidad;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SubCategoria {
    private Categoria categoria;
    private String descripcion;
    private Unidad unidad;

    public SubCategoria(
        Categoria categoria, 
        String descripcion, 
        Unidad unidad
    ) {
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.unidad = unidad;
    }
}