package com.tp.donatrack.domain.importador;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.*;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import com.tp.donatrack.repositories.DonanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * Pruebas de {@link ImportadorCargaMasiva}: alta de donantes nuevos y
 * actualización de existentes a partir de registros importados, incluyendo el
 * cambio de tipo de persona preservando su identificador.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(ImportadorCargaMasiva.class)
public class ImportadorCargaMasivaTest {

    @Autowired
    private DonanteRepository donanteRepository;

    @Autowired
    private ImportadorCargaMasiva importador;

    @Test
    @DisplayName("Al importar un donante humano que ya existe, debe actualizar nombre, apellido, documento y teléfono")
    public void actualizarDonanteSinCambioDeTipo_Humana() {
        PersonaHumana personaOriginal = new PersonaHumana("Juan", "Masculino", "Pérez", null, 30, "12345678");
        personaOriginal.agregarMedioDeContacto("EMAIL", "juan@mail.com");
        personaOriginal.agregarMedioDeContacto("SMS", "1111-1111");

        Donante donanteExistente = new Donante();
        donanteExistente.setPersona(personaOriginal);
        donanteRepository.create(donanteExistente);

        RegistroDonanteDTO registroActualizado = new RegistroDonanteDTO(
                "HUMANA", "DNI", "87654321",
                "Juan Carlos Pérez López", "juan@mail.com", "2222-2222"
        );

        importador.iniciar_migracion(List.of(registroActualizado));

        Donante donanteActualizado = donanteRepository.find("juan@mail.com");
        Assertions.assertNotNull(donanteActualizado);
        Assertions.assertInstanceOf(PersonaHumana.class, donanteActualizado.getPersona());

        PersonaHumana ph = (PersonaHumana) donanteActualizado.getPersona();
        Assertions.assertEquals("Juan", ph.getNombre(), "El nombre debería haberse actualizado");
        Assertions.assertEquals("Carlos Pérez López", ph.getApellido(), "El apellido debería haberse actualizado");
        Assertions.assertEquals("87654321", ph.getNroDocumento(), "El documento debería haberse actualizado");

        List<String> telefonos = donanteActualizado.getPersona().getMedioDeContacto().get("SMS");
        Assertions.assertEquals(1, telefonos.size());
        Assertions.assertEquals("2222-2222", telefonos.get(0), "El teléfono debería haberse actualizado");

        List<String> emails = donanteActualizado.getPersona().getMedioDeContacto().get("EMAIL");
        Assertions.assertTrue(emails.contains("juan@mail.com"), "El email original debe mantenerse");
    }

    @Test
    @DisplayName("Al importar un donante jurídico que ya existe, debe actualizar razón social y tipo de organización")
    public void actualizarDonanteSinCambioDeTipo_Juridica() {
        PersonaJuridica pjOriginal = new PersonaJuridica();
        pjOriginal.setRazonSocial("Empresa Vieja S.A.");
        pjOriginal.setTipo(TipoOrganizacion.EMPRESA);
        pjOriginal.setRubro("Comercio");
        pjOriginal.agregarMedioDeContacto("EMAIL", "contacto@empresa.com");
        pjOriginal.agregarMedioDeContacto("SMS", "3333-3333");

        Donante donanteExistente = new Donante();
        donanteExistente.setPersona(pjOriginal);
        donanteRepository.create(donanteExistente);

        RegistroDonanteDTO registroActualizado = new RegistroDonanteDTO(
                "JURIDICA", "CUIT", "30-71234567-9",
                "ONG Solidaria Nueva", "contacto@empresa.com", "4444-4444"
        );

        importador.iniciar_migracion(List.of(registroActualizado));

        Donante donanteActualizado = donanteRepository.find("contacto@empresa.com");
        Assertions.assertNotNull(donanteActualizado);
        Assertions.assertInstanceOf(PersonaJuridica.class, donanteActualizado.getPersona());

        PersonaJuridica pj = (PersonaJuridica) donanteActualizado.getPersona();
        Assertions.assertEquals("ONG Solidaria Nueva", pj.getRazonSocial(), "La razón social debería haberse actualizado");
        Assertions.assertEquals(TipoOrganizacion.ONG, pj.getTipo(), "El tipo debería haberse re-detectado como ONG");

        Assertions.assertEquals("Comercio", pj.getRubro(), "El rubro original no debería cambiar");

        List<String> telefonos = donanteActualizado.getPersona().getMedioDeContacto().get("SMS");
        Assertions.assertEquals(1, telefonos.size());
        Assertions.assertEquals("4444-4444", telefonos.get(0), "El teléfono debería haberse actualizado");
    }

    @Test
    @DisplayName("Al importar un donante que cambió de tipo (humana → jurídica), debe reemplazar la persona por una del nuevo tipo")
    public void actualizarDonanteConCambioDeTipo() {
        PersonaHumana personaOriginal = new PersonaHumana("María", "Femenino", "González", null, 25, "99999999");
        personaOriginal.agregarMedioDeContacto("EMAIL", "maria@mail.com");
        personaOriginal.agregarMedioDeContacto("SMS", "5555-5555");

        Donante donanteExistente = new Donante();
        donanteExistente.setPersona(personaOriginal);
        donanteRepository.create(donanteExistente);

        RegistroDonanteDTO registroActualizado = new RegistroDonanteDTO(
                "JURIDICA", "CUIT", "30-12345678-0",
                "María González S.R.L.", "maria@mail.com", "6666-6666"
        );

        importador.iniciar_migracion(List.of(registroActualizado));

        Donante donanteActualizado = donanteRepository.find("maria@mail.com");
        Assertions.assertNotNull(donanteActualizado);
        Assertions.assertInstanceOf(PersonaJuridica.class, donanteActualizado.getPersona(),
                "El tipo debería haber cambiado a PersonaJuridica");

        PersonaJuridica pj = (PersonaJuridica) donanteActualizado.getPersona();
        Assertions.assertEquals("María González S.R.L.", pj.getRazonSocial());
        Assertions.assertEquals(TipoOrganizacion.EMPRESA, pj.getTipo(), "Debería detectarse como EMPRESA por el S.R.L.");

        Assertions.assertNotNull(donanteActualizado.getPersona().getId(),
                "El donante recreado debe tener un identificador asignado");

        List<String> telefonos = donanteActualizado.getPersona().getMedioDeContacto().get("SMS");
        Assertions.assertEquals("6666-6666", telefonos.get(0));
        List<String> emails = donanteActualizado.getPersona().getMedioDeContacto().get("EMAIL");
        Assertions.assertTrue(emails.contains("maria@mail.com"));
    }

    @Test
    @DisplayName("Al importar un donante nuevo (email no existente), debe crearlo y retornarlo en la lista")
    public void crearDonanteNuevo() {
        RegistroDonanteDTO registroNuevo = new RegistroDonanteDTO(
                "HUMANA", "DNI", "44556677",
                "Pedro Gómez", "pedro@mail.com", "7777-7777"
        );

        List<Donante> nuevos = importador.iniciar_migracion(List.of(registroNuevo));

        Assertions.assertEquals(1, nuevos.size(), "Debería retornar 1 donante nuevo");

        Donante donanteNuevo = donanteRepository.find("pedro@mail.com");
        Assertions.assertNotNull(donanteNuevo, "El donante debería existir en el repositorio");
        Assertions.assertInstanceOf(PersonaHumana.class, donanteNuevo.getPersona());

        PersonaHumana ph = (PersonaHumana) donanteNuevo.getPersona();
        Assertions.assertEquals("Pedro", ph.getNombre());
        Assertions.assertEquals("Gómez", ph.getApellido());
        Assertions.assertEquals("44556677", ph.getNroDocumento());
    }

    @Test
    @DisplayName("Al importar un donante existente, no debe retornarlo en la lista de nuevos")
    public void actualizarDonanteNoDebeRetornarEnListaDeNuevos() {
        PersonaHumana persona = new PersonaHumana("Ana", "Femenino", "López", null, 28, "11223344");
        persona.agregarMedioDeContacto("EMAIL", "ana@mail.com");
        persona.agregarMedioDeContacto("SMS", "8888-8888");

        Donante donanteExistente = new Donante();
        donanteExistente.setPersona(persona);
        donanteRepository.create(donanteExistente);

        RegistroDonanteDTO registroExistente = new RegistroDonanteDTO(
                "HUMANA", "DNI", "99887766",
                "Ana María López Ruiz", "ana@mail.com", "9999-9999"
        );

        List<Donante> nuevos = importador.iniciar_migracion(List.of(registroExistente));

        Assertions.assertEquals(0, nuevos.size(), "No debería retornar donantes nuevos porque ya existía");
    }
}
