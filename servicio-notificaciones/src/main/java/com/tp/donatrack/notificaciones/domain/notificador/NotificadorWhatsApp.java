package com.tp.donatrack.notificaciones.domain.notificador;

import com.tp.donatrack.notificaciones.domain.notificacion.Notificacion;
import org.springframework.stereotype.Service;

@Service
public class NotificadorWhatsApp implements iNotificador {
    @Override
    public void enviarNotificacion(String nro, Notificacion notif) {
        System.out.println("Notificacion de whatsapp enviada");
    }

    @Override
    public TipoNotificador getTipo(){
        return TipoNotificador.WHATSAPP;
    }
}
