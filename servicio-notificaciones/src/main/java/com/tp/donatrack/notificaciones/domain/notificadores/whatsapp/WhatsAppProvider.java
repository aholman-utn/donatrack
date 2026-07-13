package com.tp.donatrack.notificaciones.domain.notificadores.whatsapp;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class WhatsAppProvider implements iWhatsAppProvider {
    private final WhapiWhatsApp whapiWhatsApp;

    public WhatsAppProvider(WhapiWhatsApp whapiWhatsApp) {
        this.whapiWhatsApp = whapiWhatsApp;
    }

    @Override
    public void enviarWhatsApp(String numero, String mensaje, String asunto) {
        whapiWhatsApp.enviarWhatsApp(numero, mensaje, asunto);
    }
}