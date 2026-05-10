package com.tp.donatrack.domain.entidad;

import com.tp.donatrack.domain.persona.PersonaHumana;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DonanteTest {

    @Test
    @DisplayName("Un donante debe poder vincularse con una persona humana correctamente")
    public void donanteDebeTenerUnaPersonaAsociada() {
        Donante donante = new Donante();
        PersonaHumana persona = new PersonaHumana();
        persona.setNombre("Leandro");
        persona.setApellido("Perez");

        donante.setId(1);
        donante.setPersona(persona);

        Assertions.assertEquals(1, donante.getId());
        Assertions.assertNotNull(donante.getPersona(), "La persona no debería ser nula");
        Assertions.assertEquals("Leandro", ((PersonaHumana) donante.getPersona()).getNombre());
    }

    @Test
    @DisplayName("El ID del donante debe actualizarse mediante el setter")
    public void elIdDelDonanteDebeSerModificable() {
        Donante donante = new Donante();
        donante.setId(500);

        Assertions.assertEquals(500, donante.getId());
    }
}