package com.tp.donatrack.controllers;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.services.DonanteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DonanteBusquedaTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private DonanteService donanteService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void encuentraElMismoDonantePorCorreoParcialYDniConPuntos() throws Exception {
        autenticar("ADMIN");
        PersonaHumana persona = new PersonaHumana();
        persona.setNombre("Ana");
        persona.setApellido("Pérez");
        persona.setNroDocumento("30.123.456");
        persona.agregarMedioDeContacto("EMAIL", "ana@example.test");
        Donante donante = donanteService.registrar(persona);

        mvc.perform(get("/donantes/buscar").param("email", "ANA@EXAMPLE")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(donante.getId().intValue()))
                .andExpect(jsonPath("$[0].nombre").value("Ana Pérez"))
                .andExpect(jsonPath("$[0].email").value("ana@example.test"));

        mvc.perform(get("/donantes/dni").param("dni", "30.123.456")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(donante.getId().intValue()))
                .andExpect(jsonPath("$[0].dni").value("30.123.456"));
    }

    @Test
    void restringeLaBusquedaAlPersonal() throws Exception {
        autenticar("DONANTE");

        mvc.perform(get("/donantes/buscar").param("email", "ana")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden());
    }

    private void autenticar(String rol) {
        when(jwtDecoder.decode("test-token")).thenReturn(Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("usuario-prueba")
                .claim("scope", rol)
                .build());
    }
}
