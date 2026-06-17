package com.tp.incentivos.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class MisionCompletitud extends Mision {

    private final int objetivo;
    private final String titulo;
    private final String descripcion;

    public MisionCompletitud(int objetivo, String titulo, String descripcion, Insignia insignia) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.insigniaAsociada = insignia;
    }

    @Override
    public void actualizarProgreso(InfoDonacion infoDonacion) {
        if (infoDonacion.getCategoriasAcumuladas() != null) {
            progresoActual = infoDonacion.getCategoriasAcumuladas().size();
        }
    }
}
