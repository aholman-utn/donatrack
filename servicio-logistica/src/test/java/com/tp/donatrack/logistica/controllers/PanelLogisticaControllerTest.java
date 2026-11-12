package com.tp.donatrack.logistica.controllers;

import com.tp.commons.dtos.logistica.SegmentoLogistico;
import com.tp.donatrack.logistica.clients.DonacionesPanelClient;
import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.Parada;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.repository.EnvioRepository;
import com.tp.donatrack.logistica.repository.RutaRepository;
import com.tp.donatrack.logistica.services.CamionService;
import com.tp.donatrack.logistica.services.ChoferService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PanelLogisticaControllerTest {
    @Test
    void reintentoCompletaUnaRutaParcialSinDuplicarEnviosYConservaElOrden() {
        RutaRepository rutas = mock(RutaRepository.class);
        EnvioRepository envios = mock(EnvioRepository.class);
        CamionService camiones = mock(CamionService.class);
        ChoferService choferes = mock(ChoferService.class);
        DonacionesPanelClient donaciones = mock(DonacionesPanelClient.class);
        var panel = new PanelLogisticaController(rutas, envios, camiones, choferes, donaciones);

        var solicitud = new PanelLogisticaController.CrearRuta(UUID.randomUUID(), "camion", "chofer", List.of(9L, 2L, 7L));
        String rutaId = "panel-" + solicitud.solicitudId();
        when(camiones.buscarCamionPorId("camion")).thenReturn(new Camion());
        when(choferes.buscarChoferPorId("chofer")).thenReturn(new Chofer());
        var rutasGuardadas = new HashMap<String, Ruta>();
        when(rutas.findById(rutaId)).thenAnswer(call -> Optional.ofNullable(rutasGuardadas.get(rutaId)));
        when(rutas.insert(any(Ruta.class))).thenAnswer(call -> {
            Ruta ruta = call.getArgument(0);
            rutasGuardadas.put(ruta.getId(), ruta);
            return ruta;
        });
        when(rutas.save(any(Ruta.class))).thenAnswer(call -> call.getArgument(0));
        when(donaciones.reservar(rutaId, List.of(2L, 7L, 9L))).thenReturn(List.of(
                segmento(2L, "Norte"), segmento(7L, "Sur"), segmento(9L, "Norte")));
        var enviosGuardados = new HashMap<String, Envio>();
        when(envios.save(any(Envio.class))).thenAnswer(call -> {
            Envio envio = call.getArgument(0);
            if (envio.getDonacionSegmentadaId() == 9L && !enviosGuardados.containsKey(rutaId + "-2")) {
                throw new AssertionError("La primera parada debe conservar el orden de los segmentos");
            }
            if (envio.getDonacionSegmentadaId() == 9L && enviosGuardados.size() == 1) {
                enviosGuardados.put(envio.getId(), envio);
                throw new DataAccessResourceFailureException("Se perdió la respuesta al guardar el envío");
            }
            enviosGuardados.put(envio.getId(), envio);
            return envio;
        });

        assertThrows(DataAccessResourceFailureException.class, () -> panel.crearRuta(solicitud));
        assertFalse(rutasGuardadas.get(rutaId).isPanelListo());
        assertEquals(2, enviosGuardados.size());

        Ruta completa = panel.crearRuta(solicitud);
        assertTrue(completa.isPanelListo());
        assertEquals(3, enviosGuardados.size());
        assertEquals(List.of("Norte", "Sur"), completa.getParadas().stream().map(Parada::getDireccion).toList());
        assertEquals(List.of(rutaId + "-2", rutaId + "-9"), completa.getParadas().getFirst().getEnviosIds());

        assertEquals(rutaId, panel.crearRuta(solicitud).getId());
        verify(donaciones, times(2)).reservar(rutaId, List.of(2L, 7L, 9L));
        verify(rutas, times(1)).save(any(Ruta.class));
    }

    private SegmentoLogistico segmento(long id, String direccion) {
        return new SegmentoLogistico(id, id, "Entidad", direccion, 1, "UNIDADES", "ASIGNACION_REALIZADA", null, null);
    }
}
