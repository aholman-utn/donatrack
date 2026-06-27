package com.tp.incentivos.domain.misiones;

import com.tp.incentivos.domain.Insignia;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import lombok.Getter;

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
            "Ayudaste en 2 categorías diferentes de donación"
        );
    }

    public int getObjetivo(){
        return 0;
    }

    @Override
    public boolean tieneSiguiente(){
        return true;
    }

    @Override
    public boolean estaCumplida(EntregaDonacionDTO datos) {
        return true;
    }
}
