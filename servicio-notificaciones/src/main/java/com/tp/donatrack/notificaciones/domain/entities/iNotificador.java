package com.tp.donatrack.notificaciones.domain.entities;
import com.tp.commons.domain.notificador.TipoNotificador;

public interface iNotificador {
    void enviarNotificacion(String destinatario, String mensaje, String asunto);
    TipoNotificador getMedio();
}
