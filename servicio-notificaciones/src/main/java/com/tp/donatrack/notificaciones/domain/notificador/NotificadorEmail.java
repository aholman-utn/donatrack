package com.tp.donatrack.notificaciones.domain.notificador;

import com.tp.donatrack.notificaciones.domain.notificacion.Notificacion;
import org.springframework.stereotype.Service;

@Service
public class NotificadorEmail implements iNotificador{
    @Override
    public void enviarNotificacion(String email, Notificacion notif) {
        System.out.println("---------------------EMAIL----------------------");
        System.out.println("Asunto: " + notif.getAsunto());
        System.out.println("Titulo: " + notif.getTitulo());
        System.out.println("Fecha: " + notif.getFecha());
        System.out.println("---------------------------------------------------");
        System.out.println(notif.getCuerpo());
        System.out.println("--------------------------------------------------");
        System.out.println("\n\n");
    }

    @Override
    public TipoNotificador getTipo(){
        return TipoNotificador.EMAIL;
    }
}
