package com.tp.donatrack.notificaciones.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Notificacion {
    private Long id;
    private Long id_persona;
    private String asunto;
    private String mensaje;
    private String destinatario;
    private LocalDateTime fecha;
}
