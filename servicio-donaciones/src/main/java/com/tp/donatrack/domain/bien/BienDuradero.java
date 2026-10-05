package com.tp.donatrack.domain.bien;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@Entity
@Table(name = "bienes_duraderos")
@PrimaryKeyJoinColumn(name = "id_bien")
public class BienDuradero extends Bien {

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_bien")
    private EstadoBien estado;

    protected BienDuradero() {
        super();
    }

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