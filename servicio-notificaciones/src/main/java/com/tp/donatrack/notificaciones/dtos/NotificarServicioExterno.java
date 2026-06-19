package com.tp.donatrack.notificaciones.dtos;

import com.tp.donatrack.notificaciones.domain.entities.MedioNotificador;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificarServicioExterno {
    @NotNull(message="El medio de notificacion debe ser enviado")
    MedioNotificador medio;

    @NotBlank(message="El destinatario es requerido")
    String destinatario;

    @NotBlank(message="El mensaje no puede estar vacío")
    String mensaje;

    //Optional
    String asunto;

    //TODO: Podriamos pasarlo a un uuid
    Long idPersona;
}
