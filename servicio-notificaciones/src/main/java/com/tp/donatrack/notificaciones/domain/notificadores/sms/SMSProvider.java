package com.tp.donatrack.notificaciones.domain.notificadores.sms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "simulacion", matchIfMissing = true)
public class SMSProvider implements iSMSProvider {
    @Override
    public void enviarSMS(String numero, String mensaje) {
        System.out.println("--- SIMULACIÓN: Enviando sms a " + numero + " ---");
        System.out.println("Mensaje: " + mensaje);
    }
}