package com.tp.incentivos.domain.misiones;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.domain.incentivos.Insignia;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import lombok.Getter;

@Getter
public class MisionRacha extends Mision {
    public MisionRacha(
            int objetivo,
            String titulo,
            String descripcion) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.insigniaAsociada = new Insignia(
                "Racha",
                "Donaste durante " + objetivo + " meses seguidos");
    }

    @Override
    public double calcularNuevoProgreso(EntregaDonacionDTO dto, IndicadoresDonanteDTO indicadores) {
        return (double) (100 * indicadores.getMesesConsecutivosRacha()) / this.objetivo;
    }

    @Override
    public boolean estaCumplida(EntregaDonacionDTO dto, IndicadoresDonanteDTO indicadores) {
        return indicadores.getMesesConsecutivosRacha() >= this.objetivo;
    }
}