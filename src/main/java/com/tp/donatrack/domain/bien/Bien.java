package com.tp.donatrack.domain.bien;

import com.tp.donatrack.domain.entidad.SubCategoria;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public abstract class Bien {
    protected String nombre;
    protected String descripcion;
    protected String foto;
    protected SubCategoria subCategoria;
}