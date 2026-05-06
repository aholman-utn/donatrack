package com.tp.donatrack.domain.contacto;

import com.tp.donatrack.domain.notificacion.Notificacion;

public class NotificadorTelefono implements MedioDeContacto {
    private String telefono;
    private String caracteristica;

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        // Implementación
    }
}
