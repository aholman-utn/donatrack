package com.tp.donatrack.domain.bien;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "bienes_perecederos")
@PrimaryKeyJoinColumn(name = "id_bien")
public class BienPerecedero extends Bien {

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_vencimiento")
    private Date fechaVencimiento;

    protected BienPerecedero() {
        super();
    }

    public BienPerecedero( 
        String nombre,
        String descripcion,
        String foto,
        SubCategoria subCategoria,
        Date fechaVencimiento
    ) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = foto;
        this.subCategoria = subCategoria;
        this.fechaVencimiento = fechaVencimiento;
    }  

    @Override
    public Object getCriterioSegmentacion() {
        return this.fechaVencimiento; 
    }
}