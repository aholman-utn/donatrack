package com.tp.donatrack.notificaciones.domain.notificador;

import com.tp.donatrack.notificaciones.domain.notificacion.Notificacion;
import org.springframework.stereotype.Service;

@Service
public class NotificadorTelefono implements iNotificador{

    @Override
    public void enviarNotificacion(String telefono, Notificacion notif) {
        System.out.println("Notificacion de telefono enviada");
    }

    @Override
    public TipoNotificador getTipo(){
        return TipoNotificador.SMS;
    }
}
