package com.tp.donatrack.services;

import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.notificacion.Notificacion;
import org.springframework.stereotype.Service;

@Service
public class PersonaService {
    public void guardarNotificacion(Persona persona, Notificacion notificacion) {
        if (persona != null) {
            persona.agregarNotificacion(notificacion);
        }
    }
}