package com.tp.incentivos.domain.misiones;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("COMPLETITUD")
public class MisionCompletitud extends Mision {

    public MisionCompletitud(
            int objetivo,
            String titulo,
            String descripcion
    ) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
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