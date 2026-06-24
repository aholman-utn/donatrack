package com.tp.incentivos.domain;

import lombok.*;

@Getter
public class MisionHabilDonador extends Mision {

    private final int objetivo;
    private final String titulo;
    private final String descripcion;

    public MisionHabilDonador(int objetivo, String titulo, String descripcion, Insignia insignia) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.insigniaAsociada = insignia;
    }

    @Override
    public void actualizarProgreso(InfoDonacion infoDonacion) {
        if (infoDonacion.getCantidadBienes() >= objetivo) {
            progresoActual = objetivo;
        } else {
            // Registrar el máximo alcanzado en una sola donación (sin acumular)
            progresoActual = Math.max(progresoActual, infoDonacion.getCantidadBienes());
        }
    }
}
