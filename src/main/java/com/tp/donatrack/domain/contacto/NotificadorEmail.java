package com.tp.donatrack.domain.contacto;

import lombok.Getter;
import lombok.Setter;

import com.tp.donatrack.domain.notificacion.Notificacion;

@Getter
@Setter
public class NotificadorEmail implements MedioDeContacto {
    private String correo;

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        // Implementación
    }
}
