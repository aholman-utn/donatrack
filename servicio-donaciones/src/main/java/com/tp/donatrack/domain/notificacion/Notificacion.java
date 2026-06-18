package com.tp.donatrack.domain.notificacion;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class Notificacion {
    private String titulo;
    private String cuerpo;
    private String asunto;
    private Date fecha;
    private TipoNotificacion tipo;

    public Notificacion(String titulo, String cuerpo, String asunto, TipoNotificacion tipo) {
        this.titulo = titulo;
        this.cuerpo = cuerpo;
        this.asunto = asunto;
        this.fecha = new Date();
        this.tipo = tipo;
    }
}
