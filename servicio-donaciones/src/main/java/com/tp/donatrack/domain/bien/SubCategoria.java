package com.tp.donatrack.domain.bien;

import com.tp.commons.domain.donaciones.Unidad;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class SubCategoria {
    private CategoriaBien categoria;
    private String descripcion;
    private Unidad unidad;

    public SubCategoria(
        CategoriaBien categoria, 
        String descripcion, 
        Unidad unidad
    ) {
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.unidad = unidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubCategoria other)) return false;
        if (this.descripcion == null || other.descripcion == null) return false;
        return this.descripcion.equalsIgnoreCase(other.descripcion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descripcion != null ? descripcion.toLowerCase() : null);
    }
}