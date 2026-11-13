package com.tp.donatrack.controllers;

import com.tp.commons.domain.donaciones.Unidad;
import com.tp.donatrack.clients.NotificacionesRestClient;
import com.tp.donatrack.domain.bien.*;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.DonanteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.ResourceAccessException;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortalDonanteAvisosTest {
    @Test
    void muestraLaEntregaYaConfirmadaAunqueNotificacionesNoEsteDisponible() {
        DonanteRepository donantes = mock(DonanteRepository.class);
        DonacionRepository donaciones = mock(DonacionRepository.class);
        NotificacionesRestClient notificaciones = mock(NotificacionesRestClient.class);
        var portal = new PortalDonanteController(donantes, donaciones, null, null, notificaciones);
        var persona = new PersonaHumana();
        persona.setId(4L);
        var donante = new Donante(persona);
        donante.setId(4L);
        when(donantes.findByEmail("donante@example.test")).thenReturn(List.of(donante));
        when(notificaciones.avisosDe(4L)).thenThrow(new ResourceAccessException("Sin conexión"));

        var bien = new BienDuradero("Silla", "Una silla", null,
                new SubCategoria(CategoriaBien.MOBILIARIO, "Sillas", Unidad.UNIDADES), EstadoBien.NUEVO);
        var donacion = new Donacion(donante, "Donación de prueba", new Date(), List.of(bien));
        donacion.setId(10L);
        var segmento = donacion.getDonacionesSegmentadas().getFirst();
        segmento.setId(20L);
        segmento.transicionar(EstadoDonacionSegmentada.ENTREGADA, "Entidad", "Recepción confirmada");
        when(donaciones.findByDonanteId(4L)).thenReturn(List.of(donacion));
        var jwt = Jwt.withTokenValue("prueba").header("alg", "HS256")
                .subject("donante@example.test").claim("donante_id", "4").build();

        var avisos = portal.avisos(jwt);

        assertEquals(1, avisos.size());
        assertEquals(10L, avisos.getFirst().donacionId());
        assertEquals("Tu donación fue entregada", avisos.getFirst().asunto());
        assertNotNull(avisos.getFirst().fecha());
    }
}
