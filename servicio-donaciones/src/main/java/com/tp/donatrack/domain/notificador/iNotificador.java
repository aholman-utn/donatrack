package com.tp.donatrack.domain.notificador;

import com.tp.donatrack.domain.notificacion.Notificacion;

import java.util.Map;

public interface iNotificador {
    //ejemplo de notif por email: enviarNotificacion("test@hotmail.com", Notificacion notif)
    TipoNotificador getTipo();
    void enviarNotificacion(String contacto, Notificacion notif);
}
