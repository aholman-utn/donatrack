package com.tp.donatrack.repositories;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.PersonaHumana;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica la migración JPA de la Fase 2: Donante/Persona con herencia JOINED,
 * id generado por la base (sin nextId()), y MedioDeContacto persistido como
 * tabla, incluyendo la búsqueda por valor de contacto.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DonanteRepositoryTest {

    @Autowired
    private DonanteRepository donanteRepository;

    private Donante nuevoDonante(String nombre, String email) {
        PersonaHumana persona = new PersonaHumana();
        persona.setNombre(nombre);
        persona.setApellido("Test");
        persona.agregarMedioDeContacto("EMAIL", email);
        persona.setMedioPredeterminado(Map.of("medio", "EMAIL", "valor", email));
        return new Donante(persona);
    }

    @Test
    void persisteDonanteYGeneraIdDesdeLaBase() {
        Donante guardado = donanteRepository.create(nuevoDonante("Ana", "ana@mail.com"));

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getPersona().getId()).isNotNull();
        // Donante comparte PK con Persona (MapsId).
        assertThat(guardado.getId()).isEqualTo(guardado.getPersona().getId());
    }

    @Test
    void persisteLosMediosDeContactoComoTabla() {
        Donante guardado = donanteRepository.create(nuevoDonante("Beto", "beto@mail.com"));

        Donante recuperado = donanteRepository.findByIdOrNull(guardado.getId());
        assertThat(recuperado).isNotNull();

        Map<String, List<String>> medios = recuperado.getPersona().getMedioDeContacto();
        assertThat(medios.get("EMAIL")).contains("beto@mail.com");

        Map<String, String> predeterminado = recuperado.getPersona().getMedioPredeterminado();
        assertThat(predeterminado).containsEntry("medio", "EMAIL");
        assertThat(predeterminado).containsEntry("valor", "beto@mail.com");
        assertThat(recuperado.getPersona().getTipoNotificadorPreferido()).isEqualTo(TipoNotificador.EMAIL);
    }

    @Test
    void findPorEmailDevuelveElDonante() {
        donanteRepository.create(nuevoDonante("Caro", "caro@mail.com"));

        Donante encontrado = donanteRepository.find("caro@mail.com");

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getPersona().getMedioDeContacto().get("EMAIL")).contains("caro@mail.com");
    }

    @Test
    void findPorEmailInexistenteDevuelveNull() {
        assertThat(donanteRepository.find("no-existe@mail.com")).isNull();
    }
}
