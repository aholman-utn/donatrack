package com.tp.donatrack.domain.bien;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BienPerecedero extends Bien {
    private Date fechaVencimiento;
    
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
}