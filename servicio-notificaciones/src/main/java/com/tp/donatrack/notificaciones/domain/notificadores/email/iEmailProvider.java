package com.tp.donatrack.notificaciones.domain.notificadores.email;

public interface iEmailProvider {
    void enviarEmail(String destinatario, String mensaje, String asunto);
}
