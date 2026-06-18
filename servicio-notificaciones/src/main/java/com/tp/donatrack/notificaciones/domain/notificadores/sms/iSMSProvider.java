package com.tp.donatrack.notificaciones.domain.notificadores.sms;

public interface iSMSProvider {
    void enviarSMS(String numero, String mensaje);
}
