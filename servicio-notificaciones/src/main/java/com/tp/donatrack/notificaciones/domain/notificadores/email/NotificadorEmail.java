package com.tp.donatrack.notificaciones.domain.notificadores.email;

import com.tp.commons.domain.notificador.TipoNotificador;
import org.springframework.stereotype.Component;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class NotificadorEmail implements iNotificador {

    @Autowired
    iEmailProvider emailProvider;
    
    @Override
    public void enviarNotificacion(String destinatario, String mensaje, String asunto) {
        try {
            this.emailProvider.enviarEmail(destinatario, mensaje, asunto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TipoNotificador getMedio() {
        return TipoNotificador.EMAIL;
    }

    public void cambiarProveedor(iEmailProvider nuevoProveedor){
        this.emailProvider = nuevoProveedor;
    }
}
