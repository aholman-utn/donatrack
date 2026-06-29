package com.tp.incentivos.domain.misiones;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.domain.incentivos.Insignia;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MisionCompletitud extends Mision {

    public MisionCompletitud(
            int objetivo,
            String titulo,
            String descripcion
    ) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;

        this.insigniaAsociada = new Insignia(
                "Completitud",
                "Ayudaste en " + objetivo + " categorías diferentes de donación"
        );
    }

    @Override
    public double calcularNuevoProgreso(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas) {
        return (double) (100 * metricas.getCantidadCategoriasUnicas()) / this.objetivo;
    }

    @Override
    public boolean estaCumplida(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas) {
        return metricas.getCantidadCategoriasUnicas() >= this.objetivo;
    }
}