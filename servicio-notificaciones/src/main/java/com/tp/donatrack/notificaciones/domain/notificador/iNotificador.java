package com.tp.donatrack.notificaciones.domain.notificador;

import com.tp.donatrack.notificaciones.domain.notificacion.Notificacion;

public interface iNotificador {
    //ejemplo de notif por email: enviarNotificacion("test@hotmail.com", Notificacion notif)
    TipoNotificador getTipo();
    void enviarNotificacion(String contacto, Notificacion notif);
}
