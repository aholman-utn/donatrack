package com.tp.donatrack.notificaciones.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacionInputDTO {

    private Long idPersona;

    @NotBlank(message="El destinatario es requerido")
    private String destinatario;

    // Opcional
    private String asunto;

    @NotBlank(message="El mensaje no puede estar vacío")
    private String mensaje;
}