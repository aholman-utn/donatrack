package com.tp.incentivos.domain.misiones;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.domain.incentivos.Insignia;
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

    public boolean tieneSiguiente(){
        return true;
    }

    public abstract double calcularNuevoProgreso(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas);
    public abstract boolean estaCumplida(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas);
}
