package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.domain.persona.PersonaJuridica;
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
        persona.setId(1L);

        donante.setPersona(persona);

        Assertions.assertEquals(1L, donante.getPersona().getId());
        Assertions.assertNotNull(donante.getPersona(), "La persona no debería ser nula");
        Assertions.assertEquals("Leandro", ((PersonaHumana) donante.getPersona()).getNombre());
    }

    @Test
    @DisplayName("El ID del donante debe actualizarse mediante el setter de la persona")
    public void elIdDelDonanteDebeSerModificable() {
        Donante donante = new Donante();
        PersonaHumana persona = new PersonaHumana();
        donante.setPersona(persona);
        donante.getPersona().setId(500L);

        Assertions.assertEquals(500L, donante.getPersona().getId());
    }

    @Test
    @DisplayName("Debe retornar el nombre completo del donante según el tipo de persona")
    public void testGetNombreCompleto() {
        // Persona Humana
        PersonaHumana ph = new PersonaHumana();
        ph.setNombre("Juan");
        ph.setApellido("Pérez");
        Donante donanteHumano = new Donante(ph);
        Assertions.assertEquals("Juan Pérez", donanteHumano.getNombreCompleto());

        // Persona Jurídica
        PersonaJuridica pj = new PersonaJuridica();
        pj.setRazonSocial("Fundación Ayuda");
        Donante donanteJuridico = new Donante(pj);
        Assertions.assertEquals("Fundación Ayuda", donanteJuridico.getNombreCompleto());

        // Caso Anónimo (persona nula)
        Donante donanteAnonimo = new Donante();
        Assertions.assertEquals("Donante Anónimo", donanteAnonimo.getNombreCompleto());
    }
}