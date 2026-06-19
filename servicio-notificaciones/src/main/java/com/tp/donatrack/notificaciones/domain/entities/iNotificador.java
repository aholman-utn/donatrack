package com.tp.donatrack.notificaciones.domain.entities;

public interface iNotificador {
    void enviarNotificacion(String destinatario, String mensaje, String asunto);
    MedioNotificador getMedio();
}
