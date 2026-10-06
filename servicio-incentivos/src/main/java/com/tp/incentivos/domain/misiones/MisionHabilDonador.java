package com.tp.incentivos.domain.misiones;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.domain.incentivos.Insignia;
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
@DiscriminatorValue("HABIL_DONADOR")
public class MisionHabilDonador extends Mision {

    public MisionHabilDonador(
            int objetivo,
            String titulo,
            String descripcion
    ) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        setInsigniaAsociada(new Insignia(
            "Habil Donador",
            "Hiciste una donación de gran escala"
        ));
    }

    @Override
    public boolean estaCumplida(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas) {
        return metricas.getCantidadBienesTotal() >= this.objetivo;
    }

    @Override
    public double calcularNuevoProgreso(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas) {
        return 0.0;
    }
}
