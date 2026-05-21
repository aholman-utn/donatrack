package com.tp.donatrack;

import com.tp.donatrack.domain.persona.PersonaHumana;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.tp.donatrack.domain.entidad.Donante;
import com.tp.donatrack.domain.entidad.RegistroDonante;

public class DonanteConNotificacionTest {

    @Test
    @DisplayName("Registrar un donante. El proceso de registro debe crear un nuevo donante y enviar una notificación de bienvenida")
    public void testRegistrarDonante() {
        PersonaHumana persona = new PersonaHumana();
        persona.setNombre("juan");
        persona.setApellido("perez");

        Assertions.assertTrue(persona.getNotificaciones().isEmpty(), "La persona no debería tener notificaciones");

        RegistroDonante registro = new RegistroDonante() {
        };
        Donante donante = registro.registrarDonante(persona);

        Assertions.assertFalse(donante.getPersona().getNotificaciones().isEmpty(),
                "Ahora tengo la notificacion de bienvenida");
        Assertions.assertEquals("¡Bienvenido a DonaTrack!",
                donante.getPersona().getNotificaciones().get(0).getTitulo());
    }

}