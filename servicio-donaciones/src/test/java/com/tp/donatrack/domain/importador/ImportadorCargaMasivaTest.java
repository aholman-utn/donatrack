package com.tp.donatrack.domain.importador;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.*;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import com.tp.donatrack.repositories.DonanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class ImportadorCargaMasivaTest {

    private DonanteRepository donanteRepository;
    private ImportadorCargaMasiva importador;

    @BeforeEach
    public void setUp() {
        donanteRepository = new DonanteRepository();
        importador = new ImportadorCargaMasiva(donanteRepository);
    }

    @Test
    @DisplayName("Al importar un donante humano que ya existe, debe actualizar nombre, apellido, documento y teléfono")
    public void actualizarDonanteSinCambioDeTipo_Humana() {
        // ARRANGE: crear donante humano existente
        PersonaHumana personaOriginal = new PersonaHumana("Juan", "Masculino", "Pérez", null, 30, "12345678");
        personaOriginal.agregarMedioDeContacto("email", "juan@mail.com");
        personaOriginal.agregarMedioDeContacto("telefono", "1111-1111");

        Donante donanteExistente = new Donante();
        donanteExistente.setPersona(personaOriginal);
        donanteRepository.create(donanteExistente);

        // ACT: importar CSV con los datos actualizados para el mismo email
        RegistroDonanteDTO registroActualizado = new RegistroDonanteDTO(
                "HUMANA", "DNI", "87654321",
                "Juan Carlos Pérez López", "juan@mail.com", "2222-2222"
        );

        importador.iniciar_migracion(List.of(registroActualizado));

        // ASSERT
        Donante donanteActualizado = donanteRepository.find("juan@mail.com");
        Assertions.assertNotNull(donanteActualizado);
        Assertions.assertInstanceOf(PersonaHumana.class, donanteActualizado.getPersona());

        PersonaHumana ph = (PersonaHumana) donanteActualizado.getPersona();
        Assertions.assertEquals("Juan", ph.getNombre(), "El nombre debería haberse actualizado");
        Assertions.assertEquals("Carlos Pérez López", ph.getApellido(), "El apellido debería haberse actualizado");
        Assertions.assertEquals("87654321", ph.getNroDocumento(), "El documento debería haberse actualizado");

        List<String> telefonos = donanteActualizado.getPersona().getMedioDeContacto().get("telefono");
        Assertions.assertEquals(1, telefonos.size());
        Assertions.assertEquals("2222-2222", telefonos.get(0), "El teléfono debería haberse actualizado");

        // El email no debe cambiar
        List<String> emails = donanteActualizado.getPersona().getMedioDeContacto().get("email");
        Assertions.assertTrue(emails.contains("juan@mail.com"), "El email original debe mantenerse");
    }

    @Test
    @DisplayName("Al importar un donante jurídico que ya existe, debe actualizar razón social y tipo de organización")
    public void actualizarDonanteSinCambioDeTipo_Juridica() {
        // ARRANGE: crear donante jurídico existente
        PersonaJuridica pjOriginal = new PersonaJuridica();
        pjOriginal.setRazonSocial("Empresa Vieja S.A.");
        pjOriginal.setTipo(TipoOrganizacion.EMPRESA);
        pjOriginal.setRubro("Comercio");
        pjOriginal.agregarMedioDeContacto("email", "contacto@empresa.com");
        pjOriginal.agregarMedioDeContacto("telefono", "3333-3333");

        Donante donanteExistente = new Donante();
        donanteExistente.setPersona(pjOriginal);
        donanteRepository.create(donanteExistente);

        // ACT: importar CSV con razón social actualizada
        RegistroDonanteDTO registroActualizado = new RegistroDonanteDTO(
                "JURIDICA", "CUIT", "30-71234567-9",
                "ONG Solidaria Nueva", "contacto@empresa.com", "4444-4444"
        );

        importador.iniciar_migracion(List.of(registroActualizado));

        // ASSERT
        Donante donanteActualizado = donanteRepository.find("contacto@empresa.com");
        Assertions.assertNotNull(donanteActualizado);
        Assertions.assertInstanceOf(PersonaJuridica.class, donanteActualizado.getPersona());

        PersonaJuridica pj = (PersonaJuridica) donanteActualizado.getPersona();
        Assertions.assertEquals("ONG Solidaria Nueva", pj.getRazonSocial(), "La razón social debería haberse actualizado");
        Assertions.assertEquals(TipoOrganizacion.ONG, pj.getTipo(), "El tipo debería haberse re-detectado como ONG");

        // El rubro original se conserva porque no viene en el CSV
        Assertions.assertEquals("Comercio", pj.getRubro(), "El rubro original no debería cambiar");

        List<String> telefonos = donanteActualizado.getPersona().getMedioDeContacto().get("telefono");
        Assertions.assertEquals(1, telefonos.size());
        Assertions.assertEquals("4444-4444", telefonos.get(0), "El teléfono debería haberse actualizado");
    }

    @Test
    @DisplayName("Al importar un donante que cambió de tipo (humana → jurídica), debe reemplazar la persona preservando el ID")
    public void actualizarDonanteConCambioDeTipo() {
        // ARRANGE: crear donante humano existente
        PersonaHumana personaOriginal = new PersonaHumana("María", "Femenino", "González", null, 25, "99999999");
        personaOriginal.agregarMedioDeContacto("email", "maria@mail.com");
        personaOriginal.agregarMedioDeContacto("telefono", "5555-5555");

        Donante donanteExistente = new Donante();
        donanteExistente.setPersona(personaOriginal);
        donanteRepository.create(donanteExistente);

        Long idOriginal = donanteExistente.getPersona().getId();

        // ACT: importar CSV donde ahora es una persona jurídica
        RegistroDonanteDTO registroActualizado = new RegistroDonanteDTO(
                "JURIDICA", "CUIT", "30-12345678-0",
                "María González S.R.L.", "maria@mail.com", "6666-6666"
        );

        importador.iniciar_migracion(List.of(registroActualizado));

        // ASSERT
        Donante donanteActualizado = donanteRepository.find("maria@mail.com");
        Assertions.assertNotNull(donanteActualizado);
        Assertions.assertInstanceOf(PersonaJuridica.class, donanteActualizado.getPersona(),
                "El tipo debería haber cambiado a PersonaJuridica");

        PersonaJuridica pj = (PersonaJuridica) donanteActualizado.getPersona();
        Assertions.assertEquals("María González S.R.L.", pj.getRazonSocial());
        Assertions.assertEquals(TipoOrganizacion.EMPRESA, pj.getTipo(), "Debería detectarse como EMPRESA por el S.R.L.");

        // El ID original se preserva
        Assertions.assertEquals(idOriginal, donanteActualizado.getPersona().getId(),
                "El ID de la persona debe preservarse al cambiar de tipo");

        // Teléfono y email actualizados
        List<String> telefonos = donanteActualizado.getPersona().getMedioDeContacto().get("telefono");
        Assertions.assertEquals("6666-6666", telefonos.get(0));
        List<String> emails = donanteActualizado.getPersona().getMedioDeContacto().get("email");
        Assertions.assertTrue(emails.contains("maria@mail.com"));
    }

    @Test
    @DisplayName("Al importar un donante nuevo (email no existente), debe crearlo y retornarlo en la lista")
    public void crearDonanteNuevo() {
        // ACT: importar CSV con un donante que no existe
        RegistroDonanteDTO registroNuevo = new RegistroDonanteDTO(
                "HUMANA", "DNI", "44556677",
                "Pedro Gómez", "pedro@mail.com", "7777-7777"
        );

        List<Donante> nuevos = importador.iniciar_migracion(List.of(registroNuevo));

        // ASSERT
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
        // ARRANGE: crear donante existente
        PersonaHumana persona = new PersonaHumana("Ana", "Femenino", "López", null, 28, "11223344");
        persona.agregarMedioDeContacto("email", "ana@mail.com");
        persona.agregarMedioDeContacto("telefono", "8888-8888");

        Donante donanteExistente = new Donante();
        donanteExistente.setPersona(persona);
        donanteRepository.create(donanteExistente);

        // ACT: importar CSV con el mismo email
        RegistroDonanteDTO registroExistente = new RegistroDonanteDTO(
                "HUMANA", "DNI", "99887766",
                "Ana María López Ruiz", "ana@mail.com", "9999-9999"
        );

        List<Donante> nuevos = importador.iniciar_migracion(List.of(registroExistente));

        // ASSERT
        Assertions.assertEquals(0, nuevos.size(), "No debería retornar donantes nuevos porque ya existía");
    }
}
