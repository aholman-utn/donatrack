package com.tp.donatrack.domain.trazabilidad;

import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventoTrazabilidad {

    private final EstadoDonacionSegmentada estadoAnterior;
    private final EstadoDonacionSegmentada estadoNuevo;
    private final LocalDateTime fecha;
    private final String actor;
    private final String descripcion;

    public EventoTrazabilidad(
            EstadoDonacionSegmentada estadoAnterior,
            EstadoDonacionSegmentada estadoNuevo,
            String actor,
            String descripcion
    ) {
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fecha = LocalDateTime.now();
        this.actor = actor;
        this.descripcion = descripcion;
    }

    public EventoTrazabilidad(
            EstadoDonacionSegmentada estadoAnterior,
            EstadoDonacionSegmentada estadoNuevo,
            LocalDateTime fecha,
            String actor,
            String descripcion
    ) {
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fecha = fecha;
        this.actor = actor;
        this.descripcion = descripcion;
    }
}
