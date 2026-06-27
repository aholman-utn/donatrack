package com.tp.incentivos.domain.misiones;

import com.tp.incentivos.domain.Insignia;
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
    public int getObjetivo() {
        return 0;
    }

    @Override
    public boolean tieneSiguiente(){
        return false;
    }

    @Override
    public boolean estaCumplida(EntregaDonacionDTO datos) {
        return false;
    }
}
