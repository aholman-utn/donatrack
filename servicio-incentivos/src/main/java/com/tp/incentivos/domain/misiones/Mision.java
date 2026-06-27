package com.tp.incentivos.domain.misiones;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.incentivos.domain.Insignia;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import lombok.*;

@Getter
@Setter
public abstract class Mision {
    protected Long id;
    protected int objetivo;
    protected Insignia insigniaAsociada;
    protected Nivel nivel;
    protected String titulo;
    protected String descripcion;
    protected int orden;
    public abstract int getObjetivo();

    public abstract boolean tieneSiguiente();
    public abstract boolean estaCumplida(EntregaDonacionDTO datos);
}
