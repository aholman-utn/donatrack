package com.tp.donatrack.notificaciones.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class NotificacionOutputDTO {
    Long id;
    Long id_persona;
    String titulo;
    String cuerpo;
    LocalDateTime fecha;
}
