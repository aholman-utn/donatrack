package com.tp.donatrack.notificaciones.domain.notificadores.sms;

import org.springframework.stereotype.Component;

@Component
public class SMSProvider implements iSMSProvider {
    @Override
    public void enviarSMS(String numero, String mensaje) {
        System.out.println("--- SIMULACIÓN: Enviando sms a " + numero + " ---");
        System.out.println("Mensaje: " + mensaje);
    }
}