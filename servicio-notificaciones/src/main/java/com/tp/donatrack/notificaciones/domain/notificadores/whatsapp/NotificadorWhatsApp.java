package com.tp.donatrack.notificaciones.domain.notificadores.whatsapp;


import com.tp.donatrack.notificaciones.domain.entities.MedioNotificador;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;

public class NotificadorWhatsApp implements iNotificador {

    iWhatsAppProvider whatsAppProvider;
    @Override
    public void enviarNotificacion(String numero, String mensaje) {
        try {
            whatsAppProvider.enviarWhatsApp(numero, mensaje);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MedioNotificador getMedio() {
        return MedioNotificador.WHATSAPP;
    }
}
