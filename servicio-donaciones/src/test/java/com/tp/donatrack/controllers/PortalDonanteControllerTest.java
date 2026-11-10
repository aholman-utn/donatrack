package com.tp.donatrack.controllers;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.DonanteRepository;
import com.tp.donatrack.services.DonanteService;
import com.tp.donatrack.services.EntidadBeneficiariaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortalDonanteControllerTest {
    private final DonanteRepository donantes = mock(DonanteRepository.class);
    private final DonacionRepository donaciones = mock(DonacionRepository.class);
    private final PortalDonanteController portal = new PortalDonanteController(donantes, donaciones,
            mock(DonanteService.class), mock(EntidadBeneficiariaService.class));

    @Test
    void rechazaElTokenSiElCorreoNoCoincideConElIdentificador() {
        Donante donante = new Donante();
        donante.setId(21L);
        when(donantes.findByEmail("donante@example.org")).thenReturn(List.of(donante));

        var error = assertThrows(ResponseStatusException.class, () -> portal.donaciones(token(40)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(donaciones);
    }

    @Test
    void unaPersonaJuridicaNoPuedeCambiarLaVisibilidadDeInsignias() {
        Donante donante = new Donante(new PersonaJuridica());
        donante.setId(21L);
        when(donantes.findByEmail("donante@example.org")).thenReturn(List.of(donante));

        var error = assertThrows(ResponseStatusException.class,
                () -> portal.visibilidad(token(21), new PortalDonanteController.Visibilidad(false)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertTrue(donante.getPerfil().isVisibilidadInsignia());
    }

    private Jwt token(long id) {
        return Jwt.withTokenValue("prueba").header("alg", "HS256").subject("donante@example.org")
                .claim("donante_id", Long.toString(id)).build();
    }
}
