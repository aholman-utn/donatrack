package com.tp.donatrack.tasks;

import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificador.TipoNotificador;
import com.tp.donatrack.dtos.DonanteInactivoDTO;
import com.tp.donatrack.services.DonanteService;
import com.tp.donatrack.services.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonantesInactivosCronTest {

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private DonanteService donanteService;

    @InjectMocks
    private DonantesInactivosCron donantesInactivosCron;

    @Captor
    private ArgumentCaptor<Notificacion> notificacionCaptor;

    @Test
    void debeEnviarNotificacionYGuardarEnHistorialCuandoHayDonantesInactivos() {
        DonanteInactivoDTO donanteDTO = new DonanteInactivoDTO();
        donanteDTO.setId(1);
        donanteDTO.setContacto("lautaro@test.com");
        donanteDTO.setTipoNotificadorPreferido(TipoNotificador.EMAIL);

        when(donanteService.obtenerDonantesSinInteraccionMasDeDias(20))
                .thenReturn(List.of(donanteDTO));

        donantesInactivosCron.enviarNotificacionDonantesInactivos();

        verify(notificacionService, times(1)).notificar(
                notificacionCaptor.capture(), 
                eq(TipoNotificador.EMAIL), 
                eq("lautaro@test.com")
        );

        Notificacion notificacionEnviada = notificacionCaptor.getValue();
        assertNotNull(notificacionEnviada);
        assertEquals("¡Te extrañamos en Donatrack!", notificacionEnviada.getTitulo());
        assertEquals("Inactividad", notificacionEnviada.getAsunto());

        verify(donanteService, times(1)).guardarNotificacionEnHistorial(
                eq(1), 
                any(Notificacion.class)
        );
    }

    @Test
    void noDebeHacerNadaCuandoNoHayDonantesInactivos() {
        when(donanteService.obtenerDonantesSinInteraccionMasDeDias(20))
                .thenReturn(Collections.emptyList());

        donantesInactivosCron.enviarNotificacionDonantesInactivos();

        verify(notificacionService, never()).notificar(any(), any(), any());
        verify(donanteService, never()).guardarNotificacionEnHistorial(any(), any());
    }

    @Test
    void debeContinuarConElSiguienteSiFallaUnoYNoGuardarHistorialDelFallido() {
        DonanteInactivoDTO donante1 = new DonanteInactivoDTO();
        donante1.setId(1);
        donante1.setContacto("falla@test.com");
        donante1.setTipoNotificadorPreferido(TipoNotificador.WHATSAPP);

        DonanteInactivoDTO donante2 = new DonanteInactivoDTO();
        donante2.setId(2);
        donante2.setContacto("exito@test.com");
        donante2.setTipoNotificadorPreferido(TipoNotificador.EMAIL);

        when(donanteService.obtenerDonantesSinInteraccionMasDeDias(20))
                .thenReturn(List.of(donante1, donante2));

        doThrow(new RuntimeException("Error de conexión RestTemplate")).when(notificacionService)
                .notificar(any(Notificacion.class), eq(TipoNotificador.WHATSAPP), eq("falla@test.com"));

        donantesInactivosCron.enviarNotificacionDonantesInactivos();

        verify(notificacionService, times(1)).notificar(
                any(Notificacion.class), 
                eq(TipoNotificador.EMAIL), 
                eq("exito@test.com")
        );
        
        verify(donanteService, times(1)).guardarNotificacionEnHistorial(
                eq(2), 
                any(Notificacion.class)
        );

        verify(donanteService, never()).guardarNotificacionEnHistorial(
                eq(1), 
                any(Notificacion.class)
        );
    }
}