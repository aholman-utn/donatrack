package com.tp.incentivos.domain.misiones;

import com.tp.incentivos.domain.Insignia;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import lombok.*;

@Getter
@AllArgsConstructor
public class MisionRacha extends Mision {
    public MisionRacha(
            int objetivo,
            String titulo,
            String descripcion
    ) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.insigniaAsociada =  new Insignia(
            "Racha",
            "Donaste durante 2 meses seguidos"
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
