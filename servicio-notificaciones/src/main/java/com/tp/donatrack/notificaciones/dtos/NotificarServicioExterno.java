package com.tp.donatrack.notificaciones.dtos;


import com.tp.donatrack.notificaciones.domain.entities.MedioNotificador;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class NotificarServicioExterno {
    @NotBlank(message="El medio de notificacion debe ser enviado")
    MedioNotificador medio;

    @NotBlank(message="El destinatario es requerido")
    String destinatario;

    @NotBlank(message="El mensaje no puede estar vacío")
    String mensaje;
}
