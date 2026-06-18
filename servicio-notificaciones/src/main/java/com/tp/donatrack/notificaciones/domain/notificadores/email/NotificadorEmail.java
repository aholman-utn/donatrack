package com.tp.donatrack.notificaciones.domain.notificadores.email;


import com.tp.donatrack.notificaciones.domain.entities.MedioNotificador;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;
import org.springframework.beans.factory.annotation.Autowired;

public class NotificadorEmail implements iNotificador {

    @Autowired
    iEmailProvider emailProvider;
    @Override
    public void enviarNotificacion(String destinatario, String mensaje) {
        try {
            this.emailProvider.enviarEmail(destinatario, mensaje);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MedioNotificador getMedio() {
        return MedioNotificador.EMAIL;
    }

    public void cambiarProveedor(iEmailProvider nuevoProveedor){
        this.emailProvider = nuevoProveedor;
    }
}
