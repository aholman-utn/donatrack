package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.notificador.TipoNotificador;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionRequestDTO {
    @NotNull(message="El medio de notificacion debe ser enviado")
    TipoNotificador medio;

    @NotBlank(message="El destinatario es requerido")
    String destinatario;

    @NotBlank(message="El mensaje no puede estar vacío")
    String mensaje;

    //Optional
    String asunto;

    //TODO: Podriamos pasarlo a un uuid
    Long idPersona;
}