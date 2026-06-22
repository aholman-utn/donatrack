package com.tp.donatrack.notificaciones.domain.notificadores.sms;

import com.tp.commons.domain.notificador.TipoNotificador;
import org.springframework.stereotype.Component;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;

@Component
public class NotificadorSMS implements iNotificador {

    private final iSMSProvider smsProvider;

    public NotificadorSMS(iSMSProvider smsProvider) {
        this.smsProvider = smsProvider;
    }

    @Override
    public void enviarNotificacion(String numero, String mensaje, String asunto) {
        try {
            smsProvider.enviarSMS(numero, mensaje);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TipoNotificador getMedio() {
        return TipoNotificador.SMS;
    }
}
