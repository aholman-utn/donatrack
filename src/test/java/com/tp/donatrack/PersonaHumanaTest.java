package com.tp.donatrack;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.tp.donatrack.domain.persona.PersonaHumana;

import java.util.Date;
import java.util.Calendar;

public class PersonaHumanaTest {

    @Test
    public void sePuedeInstanciarUnaPersona() {
        String nombre = "Leando";
        String apellido = "Perez";
        String genero = "Masculino";
        int nroDocumento = 12345678;
        int edad = 25;
        Date fechaNac = new Calendar.Builder().setDate(2000, 0, 1).build().getTime();

        PersonaHumana persona = PersonaHumana.builder()
            .nombre(nombre)
            .genero(genero)
            .apellido(apellido)
            .edad(edad)
            .nroDocumento(String.valueOf(nroDocumento)) // Convertimos el int del test a String
            .build();

        Assertions.assertEquals(nombre, persona.getNombre());
        Assertions.assertEquals(apellido, persona.getApellido());
        Assertions.assertEquals(String.valueOf(nroDocumento), persona.getNroDocumento());
    }

    @Test
    public void sePuedenModificarLosAtributosConSetters() {
        PersonaHumana persona = new PersonaHumana();
        String nuevoNombre = "Juan";

        persona.setNombre(nuevoNombre);
        persona.setEdad(30);

        Assertions.assertEquals("Juan", persona.getNombre());
        Assertions.assertEquals(30, persona.getEdad());
    }
}
