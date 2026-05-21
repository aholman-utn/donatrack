package com.tp.donatrack;

import com.tp.donatrack.domain.entidad.Donante;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.repositories.DonanteRepository;
import com.tp.donatrack.services.DonanteService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class DonanteServiceTest {

    @Test
    void importarCSVActualizaDonanteExistentePorEmail() {
        DonanteRepository repository = new DonanteRepository();
        DonanteService service = new DonanteService(repository);

        service.importarCSV(csv(String.join("\n",
                "tipoPersona,tipoDoc,documento,nombre,email,telefono",
                "HUMANA,DNI,12345678,Juan Perez,juan@example.com,111"
        )));

        service.importarCSV(csv(String.join("\n",
                "tipoPersona,tipoDoc,documento,nombre,email,telefono",
                "HUMANA,DNI,87654321,Juan Gomez,juan@example.com,222"
        )));

        List<Donante> donantes = repository.findAll();
        PersonaHumana persona = assertInstanceOf(PersonaHumana.class, donantes.getFirst().getPersona());

        assertEquals(1, donantes.size());
        assertEquals("Juan", persona.getNombre());
        assertEquals("Gomez", persona.getApellido());
        assertEquals("87654321", persona.getNroDocumento());
        assertEquals("222", persona.getMediosDeContacto().get("telefono"));
    }

    private MockMultipartFile csv(String contenido) {
        return new MockMultipartFile(
                "file",
                "donantes.csv",
                "text/csv",
                contenido.getBytes(StandardCharsets.UTF_8)
        );
    }
}
