package com.tp.donatrack.notificaciones.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacionInputDTO {

    Long id_persona;
    String titulo;
    String cuerpo;
}
