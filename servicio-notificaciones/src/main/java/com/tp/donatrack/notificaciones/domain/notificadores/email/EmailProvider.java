package com.tp.donatrack.notificaciones.domain.notificadores.email;

import org.springframework.stereotype.Component;

@Component
public class EmailProvider implements iEmailProvider {

    @Override
    public void enviarEmail(String destinatario, String mensaje, String asunto) {
        System.out.println("--- SIMULACIÓN: Enviando email a " + destinatario + " ---");
        System.out.println("Asunto: " + asunto);
        System.out.println("Mensaje: " + mensaje);
    }
}