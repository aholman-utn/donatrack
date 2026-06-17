package com.tp.incentivos.domain;

import lombok.*;

@Getter
@AllArgsConstructor
public class MisionHabilDonador extends Mision {

    private final int objetivo;
    private final String titulo;
    private final String descripcion;
    private Insignia insignia;

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
