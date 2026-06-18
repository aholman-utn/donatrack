package com.tp.donatrack.notificaciones.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Notificacion {
    private Long id;
    private String titulo;
    private String cuerpo;
    private LocalDateTime fecha;
    private Long id_persona;
}
