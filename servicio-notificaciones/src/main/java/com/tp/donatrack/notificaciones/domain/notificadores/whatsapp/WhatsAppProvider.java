package com.tp.donatrack.notificaciones.domain.notificadores.whatsapp;

import org.springframework.stereotype.Component;

@Component
public class WhatsAppProvider implements iWhatsAppProvider {
    @Override
    public void enviarWhatsApp(String numero, String mensaje) {
        System.out.println("--- SIMULACIÓN: Enviando whatsapp a " + numero + " ---");
        System.out.println("Mensaje: " + mensaje);
    }
}