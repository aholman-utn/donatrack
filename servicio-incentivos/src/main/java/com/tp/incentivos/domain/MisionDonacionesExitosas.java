package com.tp.incentivos.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MisionDonacionesExitosas extends Mision {

    private final int objetivo;
    private final String titulo;
    private final String descripcion;
    private Insignia insignia;

    @Override
    public void actualizarProgreso(InfoDonacion infoDonacion) {
        progresoActual++;
    }
}
