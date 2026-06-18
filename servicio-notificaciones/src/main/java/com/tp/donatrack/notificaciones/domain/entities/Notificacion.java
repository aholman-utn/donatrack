package com.tp.donatrack.notificaciones.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Notificacion {
    Long id;
    String titulo;
    String cuerpo;
    LocalDateTime fecha;
    Long id_persona;
}
