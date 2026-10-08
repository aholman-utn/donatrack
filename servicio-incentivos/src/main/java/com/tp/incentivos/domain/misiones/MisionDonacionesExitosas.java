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
@DiscriminatorValue("DONACIONES_EXITOSAS")
public class MisionDonacionesExitosas extends Mision {

    public MisionDonacionesExitosas(
            int objetivo,
            String titulo,
            String descripcion) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
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
