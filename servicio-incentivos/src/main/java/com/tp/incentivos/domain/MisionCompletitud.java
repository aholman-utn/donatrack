package com.tp.incentivos.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MisionCompletitud extends Mision {

    private final int objetivo;
    private final String titulo;
    private final String descripcion;
    private Insignia insignia;

    @Override
    public void actualizarProgreso(InfoDonacion infoDonacion) {
        if (infoDonacion.getCategoriasAcumuladas() != null) {
            progresoActual = infoDonacion.getCategoriasAcumuladas().size();
        }
    }
}
