package com.tp.donatrack.domain.persona;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.Calendar;

public class PersonaHumanaTest {

    @Test
    public void sePuedeInstanciarUnaPersona() {
        String nombre = "Leando";
        String apellido = "Perez";
        String genero = "Masculino";
        String nroDocumento = "12345678";
        int edad = 25;
        Date fechaNac = new Calendar.Builder().setDate(2000, 0, 1).build().getTime();

        PersonaHumana persona = new PersonaHumana(
            nombre, 
            genero, 
            apellido, 
            fechaNac, 
            edad, 
            nroDocumento
        );

        Assertions.assertEquals(nombre, persona.getNombre());
        Assertions.assertEquals(apellido, persona.getApellido());
        Assertions.assertEquals(nroDocumento, persona.getNroDocumento());
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