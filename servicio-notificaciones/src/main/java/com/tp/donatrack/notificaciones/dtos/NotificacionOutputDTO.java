package com.tp.donatrack.notificaciones.dtos;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionOutputDTO {
    Long id;
    Long id_persona;
    String titulo;
    String cuerpo;
    LocalDateTime fecha;
}
