package com.tp.donatrack.notificaciones.domain.notificacion;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Notificacion {
    private String titulo;
    private String cuerpo;
    private String asunto;
    private LocalDateTime fecha;
    //private TipoNotificacion tipo; //No es importante para el servicio de notificaciones si el mensaje es por bienvenida, inactividad, etc.

    public Notificacion(String titulo, String cuerpo, String asunto) {
        this.titulo = titulo;
        this.cuerpo = cuerpo;
        this.asunto = asunto;
        this.fecha = LocalDateTime.now();
    }
}
