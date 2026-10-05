package com.tp.donatrack.domain.bien;

import com.tp.commons.domain.donaciones.Unidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "subcategorias")
public class SubCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subcategoria")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_bien")
    private CategoriaBien categoria;

    @Column(name = "descripcion")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad")
    private Unidad unidad;

    protected SubCategoria() {
    }

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
