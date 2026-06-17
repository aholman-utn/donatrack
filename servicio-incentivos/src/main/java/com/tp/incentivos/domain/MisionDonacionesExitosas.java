package com.tp.incentivos.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class MisionDonacionesExitosas extends Mision {

    private final int objetivo;
    private final String titulo;
    private final String descripcion;

    public MisionDonacionesExitosas(int objetivo, String titulo, String descripcion, Insignia insignia) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.insigniaAsociada = insignia;
    }

    @Override
    public void actualizarProgreso(InfoDonacion infoDonacion) {
        progresoActual++;
    }
}
