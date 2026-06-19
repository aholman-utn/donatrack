package com.tp.donatrack.notificaciones.dtos;

import jakarta.validation.constraints.NotBlank;
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
    Long idPersona;
    String asunto;
    String mensaje;
    String destinatario;
    LocalDateTime fecha;
}
