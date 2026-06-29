package com.tp.incentivos.domain.misiones;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.domain.incentivos.Insignia;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import lombok.*;

@Getter
public class MisionHabilDonador extends Mision {
    public MisionHabilDonador(
            int objetivo,
            String titulo,
            String descripcion
    ) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.insigniaAsociada = new Insignia(
            "Habil Donador",
            "Hiciste una donación de gran escala"
        );
    }

    @Override
    public boolean estaCumplida(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas) {
        return metricas.getCantidadBienesTotal() >= this.objetivo;
    }

    //Hábil Donador: donación que supere X cantidad de bienes.
    @Override
    public double calcularNuevoProgreso(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas) {
        //El progreso sera 0.0 porque la misión se cumple o no se cumple, no tiene progreso.
        return 0.0;
    }
}
