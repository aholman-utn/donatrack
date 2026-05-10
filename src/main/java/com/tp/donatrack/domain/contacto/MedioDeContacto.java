package com.tp.donatrack.domain.contacto;

import com.tp.donatrack.domain.notificacion.Notificacion;

public interface MedioDeContacto {
    void enviarNotificacion(Notificacion notificacion);
}
