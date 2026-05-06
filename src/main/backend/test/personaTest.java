package test;

import domain.persona.PersonaHumana;
import domain.ubicacion.Direccion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PersonaTest {

    @Test
    public void testCrearPersonaHumanaConDireccion() {
        Direccion direccion = new Direccion();
        direccion.setCalle1("una calle falsa");
        direccion.setAltura(123);

        PersonaHumana persona = new PersonaHumana();
        persona.setNombre("Lucas");
        persona.setApellido("Perez");
        persona.setDireccion(direccion); 

        assertNotNull(persona.getDireccion(), "La dirección no debería ser nula");
        assertEquals("Lucas", persona.getNombre());
        assertEquals("una calle falsa", persona.getDireccion().getCalle1());
        assertEquals(123, persona.getDireccion().getAltura());
        
        System.out.println("Test exitoso: Persona creada con nombre " + persona.getNombre() + 
                           " viviendo en " + persona.getDireccion().getCalle1() + " " + persona.getDireccion().getAltura());
    }
}
