package com.tp.donatrack.domain.entidad;

import java.util.Date;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificacion.TipoNotificacion;
import com.tp.donatrack.domain.persona.Persona;

public class RegistroDonante {
    public Donante registrarDonante(Persona persona) {
        Donante nuevoDonante = new Donante();
        nuevoDonante.setPersona(persona);

        this.notificarBienvenida(persona);
        return nuevoDonante;
    }

    private void notificarBienvenida(Persona persona) {

        Notificacion bienvenida = new Notificacion();
        bienvenida.setTitulo("¡Bienvenido a DonaTrack!");
        bienvenida.setCuerpo("Gracias perri por registrarte ;)");
        bienvenida.setAsunto("Confirmación de Registro");
        bienvenida.setFecha(new Date());
        bienvenida.setTipo(TipoNotificacion.BIENVENIDA);

        persona.agregarNotificacion(bienvenida);

        if (persona.getMedioPredeterminado() != null) {
            persona.getMedioPredeterminado().enviarNotificacion(bienvenida);
        }
    }
}
