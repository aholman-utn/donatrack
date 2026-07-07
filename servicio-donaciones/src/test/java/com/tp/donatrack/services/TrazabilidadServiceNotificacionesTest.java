package com.tp.donatrack.services;

import com.tp.commons.domain.donaciones.Unidad;
import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.services.notificador.NotificacionQueueClient;
import com.tp.donatrack.domain.bien.*;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.DonanteRepository;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class TrazabilidadServiceNotificacionesTest {

    @Mock
    private NotificacionQueueClient notificacionQueueClient;

    private DonacionRepository donacionRepository;
    private DonanteRepository donanteRepository;
    private EntidadBeneficiariaRepository entidadBeneficiariaRepository;
    private TrazabilidadService trazabilidadService;

    private Donacion donacion;
    private Donante donante;
    private EntidadBeneficiaria entidad;

    @BeforeEach
    void setUp() {
        donacionRepository = new DonacionRepository();
        donanteRepository = new DonanteRepository();
        entidadBeneficiariaRepository = new EntidadBeneficiariaRepository();

        trazabilidadService = new TrazabilidadService(
                donacionRepository,
                donanteRepository,
                entidadBeneficiariaRepository,
                notificacionQueueClient
        );

        PersonaHumana personaDonante = new PersonaHumana();
        personaDonante.setNombre("Juan");
        personaDonante.setApellido("Pérez");
        personaDonante.setMedioPredeterminado(Map.of("medio", "EMAIL", "valor", "juan@mail.com"));
        donante = new Donante(personaDonante);
        donanteRepository.create(donante);

        PersonaJuridica personaEntidad = new PersonaJuridica();
        personaEntidad.setRazonSocial("Comedor Los Pibes");
        personaEntidad.setMedioPredeterminado(Map.of("medio", "EMAIL", "valor", "comedor@mail.com"));
        entidad = new EntidadBeneficiaria(personaEntidad);
        entidadBeneficiariaRepository.create(entidad);

        SubCategoria subCategoria = new SubCategoria(CategoriaBien.ALIMENTOS, "Fideos", Unidad.KG);
        BienPerecedero bien = new BienPerecedero("Fideos", "Fideos secos 500g", null, subCategoria, new Date());
        List<Bien> bienes = List.of(bien);
        donacion = new Donacion(donante, "Donación de fideos", new Date(), bienes);
        donacionRepository.save(donacion);

        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        segmento.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");
        segmento.setEntidadBeneficiariaAsignadaId(entidad.getDatosDeEntidad().getId());
        segmento.listarParaEntrega("Logística");
    }

    @Test
    @DisplayName("notificarInicioDeRuta envía notificación al donante y a la entidad beneficiaria")
    void notificaInicioDeRuta() {
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        segmento.iniciarTraslado("Sistema (Logística Polling)");

        trazabilidadService.notificarInicioDeRuta(segmento);

        verify(notificacionQueueClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("juan@mail.com"),
                argThat(s -> s.contains("ya inició la ruta de entrega")),
                eq("Tu donación está en camino"),
                eq(donante.getPersona().getId())
        );

        verify(notificacionQueueClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("comedor@mail.com"),
                argThat(s -> s.contains("en traslado hacia tu ubicación")),
                eq("Una donación está en camino hacia tu entidad"),
                eq(entidad.getDatosDeEntidad().getId())
        );
    }

    @Test
    @DisplayName("recepcionarEntrega notifica al donante y a la entidad beneficiaria")
    void notificaEntregaExitosa() {
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().getFirst();
        Integer idDonacion = donacion.getId();
        Integer idSegmento = Math.toIntExact(segmento.getId());

        segmento.iniciarTraslado("Chofer");

        trazabilidadService.recepcionarEntrega(idDonacion, idSegmento);

        verify(notificacionQueueClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("juan@mail.com"),
                argThat(s -> s.contains("entregada exitosamente")),
                eq("¡Tu donación fue entregada con éxito!"),
                eq(donante.getPersona().getId())
        );

        verify(notificacionQueueClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("comedor@mail.com"),
                argThat(s -> s.contains("entregada y confirmada")),
                eq("Entrega recibida exitosamente"),
                eq(entidad.getDatosDeEntidad().getId())
        );
    }

    @Test
    @DisplayName("notificarEntregaNoSatisfactoria envía notificación con justificación al donante y a la entidad")
    void notificaEntregaNoSatisfactoria() {
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        segmento.iniciarTraslado("Chofer");

        String justificacion = "Tocamos timbre pero nadie respondió";
        segmento.registrarEntregaFallida("Sistema (Logística)", justificacion);

        trazabilidadService.notificarEntregaNoSatisfactoria(segmento, justificacion);

        verify(notificacionQueueClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("juan@mail.com"),
                argThat(s -> s.contains("Tocamos timbre pero nadie respondió")),
                eq("Hubo un problema con la entrega de tu donación"),
                eq(donante.getPersona().getId())
        );

        verify(notificacionQueueClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("comedor@mail.com"),
                argThat(s -> s.contains("Tocamos timbre pero nadie respondió")),
                eq("La entrega no pudo completarse"),
                eq(entidad.getDatosDeEntidad().getId())
        );
    }

    @Test
    @DisplayName("Si falla el envío de notificación, no se lanza excepción (fire-and-forget)")
    void falloEnNotificacionNoLanzaExcepcion() {
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        segmento.iniciarTraslado("Chofer");

        when(notificacionQueueClient.notificar(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Servicio de notificaciones no disponible"));

        assertDoesNotThrow(() ->
                trazabilidadService.notificarInicioDeRuta(segmento)
        );
    }

    @Test
    @DisplayName("Si la donación no tiene donante, no se intenta notificar")
    void sinDonanteNoNotifica() {
        SubCategoria sub = new SubCategoria(CategoriaBien.ALIMENTOS, "Arroz", Unidad.KG);
        BienPerecedero bien = new BienPerecedero("Arroz", "Arroz 1kg", null, sub, new Date());
        Donacion donacionSinDonante = new Donacion(null, "Donación anónima", new Date(), List.of(bien));
        donacionRepository.save(donacionSinDonante);

        DonacionSegmentada segmento = donacionSinDonante.getDonacionesSegmentadas().get(0);
        segmento.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");
        segmento.listarParaEntrega("Logística");
        segmento.iniciarTraslado("Chofer");

        trazabilidadService.notificarInicioDeRuta(segmento);

        verify(notificacionQueueClient, never()).notificar(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Si el donante no tiene contacto predeterminado, no se envía notificación")
    void sinContactoPredeterminadoNoNotifica() {
        PersonaHumana personaSinContacto = new PersonaHumana();
        personaSinContacto.setNombre("María");
        personaSinContacto.setApellido("López");
        Donante donanteSinContacto = new Donante(personaSinContacto);
        donanteRepository.create(donanteSinContacto);

        SubCategoria sub = new SubCategoria(CategoriaBien.VESTIMENTA, "Camperas", Unidad.UNIDADES);
        BienDuradero bien = new BienDuradero("Campera", "Campera de abrigo", null, sub, EstadoBien.NUEVO);
        Donacion donacionNueva = new Donacion(donanteSinContacto, "Camperas", new Date(), List.of(bien));
        donacionRepository.save(donacionNueva);

        DonacionSegmentada segmento = donacionNueva.getDonacionesSegmentadas().get(0);
        segmento.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");
        segmento.listarParaEntrega("Logística");
        segmento.iniciarTraslado("Chofer");

        trazabilidadService.notificarInicioDeRuta(segmento);

        verify(notificacionQueueClient, never()).notificar(any(), any(), any(), any(), any());
    }
}
