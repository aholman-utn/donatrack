package com.tp.donatrack.notificaciones.domain.notificadores.sms;


import com.tp.donatrack.notificaciones.domain.entities.MedioNotificador;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;

public class NotificadorSMS implements iNotificador {

    iSMSProvider smsProvider;
    @Override
    public void enviarNotificacion(String numero, String mensaje) {
        try {
            smsProvider.enviarSMS(numero, mensaje);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MedioNotificador getMedio() {
        return MedioNotificador.SMS;
    }
}
