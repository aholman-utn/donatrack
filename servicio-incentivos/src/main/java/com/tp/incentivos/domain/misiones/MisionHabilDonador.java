package com.tp.incentivos.domain.misiones;

import com.tp.incentivos.domain.Insignia;
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
    public int getObjetivo() {
        return 0;
    }

    @Override
    public boolean tieneSiguiente(){
        return true;
    }

    @Override
    public boolean estaCumplida(EntregaDonacionDTO datos) {
        return false;
    }
}
