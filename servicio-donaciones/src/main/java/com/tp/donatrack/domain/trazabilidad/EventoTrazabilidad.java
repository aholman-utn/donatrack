package com.tp.donatrack.domain.trazabilidad;

import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentadaConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "eventos_trazabilidad")
public class EventoTrazabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento_trazabilidad")
    private Long id;

    @Convert(converter = EstadoDonacionSegmentadaConverter.class)
    @Column(name = "estado_anterior")
    private EstadoDonacionSegmentada estadoAnterior;

    @Convert(converter = EstadoDonacionSegmentadaConverter.class)
    @Column(name = "estado_nuevo")
    private EstadoDonacionSegmentada estadoNuevo;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "actor")
    private String actor;

    @Column(name = "descripcion")
    private String descripcion;

    // Requerido por JPA.
    protected EventoTrazabilidad() {
    }

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
