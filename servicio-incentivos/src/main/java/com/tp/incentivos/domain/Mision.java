package com.tp.incentivos.domain;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
public abstract class Mision {

    protected int progresoActual = 0;
    protected boolean completada = false;
    protected LocalDate fechaObtencion = null;
    protected Insignia insigniaAsociada;
    // protected final String titulo;
    // protected final String descripcion;

    public abstract int getObjetivo();

    public abstract String getTitulo();

    public abstract String getDescripcion();

    public abstract void actualizarProgreso(InfoDonacion infoDonacion);

    public boolean evaluarYMarcarCompletitud() {
        if (!completada && progresoActual >= getObjetivo()) {
            this.completada = true;
            this.fechaObtencion = LocalDate.now();
            // TODO ACA hacer el POST a n8n necesito
            // TODO ACA poner el notificador para cuando se completa la mision
            return true;
        }
        return false;
    }

    public boolean puedePerderProgreso() {
        return false;
    }
}
