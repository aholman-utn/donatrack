package com.tp.donatrack.notificaciones.domain.notificadores.whatsapp;

public interface iWhatsAppProvider {

    void enviarWhatsApp(String numero, String mensaje, String asunto);
}
