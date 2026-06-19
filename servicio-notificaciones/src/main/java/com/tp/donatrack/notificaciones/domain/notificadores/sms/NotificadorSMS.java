package com.tp.donatrack.notificaciones.domain.notificadores.sms;

import org.springframework.stereotype.Component;
import com.tp.donatrack.notificaciones.domain.entities.MedioNotificador;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class NotificadorSMS implements iNotificador {

    @Autowired
    iSMSProvider smsProvider;

    @Override
    public void enviarNotificacion(String numero, String mensaje, String asunto) {
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
