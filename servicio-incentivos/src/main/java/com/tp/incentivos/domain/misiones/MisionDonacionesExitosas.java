package com.tp.incentivos.domain.misiones;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.domain.incentivos.Insignia;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import lombok.Getter;

@Getter
public class MisionDonacionesExitosas extends Mision {
    public MisionDonacionesExitosas(
            int objetivo,
            String titulo,
            String descripcion
    ) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.insigniaAsociada = new Insignia(
                "Donacion Exitosa",
                "Completaste tus primeras 3 donaciones exitosas"
        );
    }

    @Override
    public boolean tieneSiguiente(){
        return false;
    }

    @Override
    public double calcularNuevoProgreso(EntregaDonacionDTO dto, IndicadoresDonanteDTO indicadores) {
        return dto.getProgreso() + 1;
    }

    @Override
    public boolean estaCumplida(EntregaDonacionDTO datos, IndicadoresDonanteDTO metricas) {
        return metricas.getCantidadDonacionesEntregadas() >= this.objetivo;
    }
}
