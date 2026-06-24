package com.tp.donatrack.notificaciones.domain.notificadores.whatsapp;

import com.tp.commons.domain.notificador.TipoNotificador;
import org.springframework.stereotype.Component;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;

@Component
public class NotificadorWhatsApp implements iNotificador {

    private final iWhatsAppProvider whatsAppProvider;

    public NotificadorWhatsApp(iWhatsAppProvider whatsAppProvider) {
        this.whatsAppProvider = whatsAppProvider;
    }

    @Override
    public void enviarNotificacion(String numero, String mensaje, String asunto) {
        try {
            whatsAppProvider.enviarWhatsApp(numero, mensaje);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TipoNotificador getMedio() {
        return TipoNotificador.WHATSAPP;
    }
}
