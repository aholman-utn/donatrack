package com.tp.commons.dtos.notificador;

import com.tp.commons.domain.notificador.TipoNotificador;
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
    private TipoNotificador medio;

    @NotBlank(message="El destinatario es requerido")
    private String destinatario;

    @NotBlank(message="El mensaje no puede estar vacío")
    private String mensaje;

    //Optional
    private String asunto;

    //TODO: Podriamos pasarlo a un uuid
    private Long idPersona;
}