package com.tp.donatrack.services;

import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificador.TipoNotificador;
import com.tp.donatrack.domain.notificador.iNotificador;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private final Map<TipoNotificador, iNotificador> notificadores;

    public NotificacionService(List<iNotificador> lista) {
        this.notificadores = lista.stream()
                .collect(Collectors.toMap(
                        iNotificador::getTipo,
                        n -> n
                ));
    }

    public void notificar(Notificacion notif, TipoNotificador tipo, String contacto) {
        iNotificador notificador = notificadores.get(tipo);

        if (notificador == null) {
            throw new IllegalArgumentException("No existe notificador: " + tipo);
        }

        notificador.enviarNotificacion(contacto, notif);
    }
}
