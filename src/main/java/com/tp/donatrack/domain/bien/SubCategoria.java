package com.tp.donatrack.domain.bien;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubCategoria subCategoraiO)) return false;
        return Objects.equals(categoria, subCategoraiO.categoria) &&
                Objects.equals(descripcion, subCategoraiO.descripcion) &&
                Objects.equals(unidad, subCategoraiO.unidad);
    }
}