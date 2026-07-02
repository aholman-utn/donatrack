package com.tp.donatrack.services;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.services.notificador.NotificacionRestClient;
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
    private NotificacionRestClient notificacionRestClient;

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
                notificacionRestClient
        );

        // Crear donante con medio de contacto predeterminado
        PersonaHumana personaDonante = new PersonaHumana();
        personaDonante.setNombre("Juan");
        personaDonante.setApellido("Pérez");
        personaDonante.setMedioPredeterminado(Map.of("medio", "EMAIL", "valor", "juan@mail.com"));
        donante = new Donante(personaDonante);
        donanteRepository.create(donante);

        // Crear entidad beneficiaria con medio de contacto predeterminado
        PersonaJuridica personaEntidad = new PersonaJuridica();
        personaEntidad.setRazonSocial("Comedor Los Pibes");
        personaEntidad.setMedioPredeterminado(Map.of("medio", "EMAIL", "valor", "comedor@mail.com"));
        entidad = new EntidadBeneficiaria(personaEntidad);
        entidadBeneficiariaRepository.create(entidad);

        // Crear donación con bienes
        SubCategoria subCategoria = new SubCategoria(CategoriaBien.ALIMENTOS, "Fideos", Unidad.KILOGRAMOS);
        BienPerecedero bien = new BienPerecedero("Fideos", "Fideos secos 500g", null, subCategoria, new Date());
        List<Bien> bienes = List.of(bien);
        donacion = new Donacion(donante, "Donación de fideos", new Date(), bienes);
        donacionRepository.save(donacion);

        // Asignar la donación a la entidad (simular flujo completo previo)
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        segmento.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");
        segmento.setEntidadBeneficiariaAsignadaId(entidad.getDatosDeEntidad().getId());
        segmento.listarParaEntrega("Logística");
    }

    @Test
    @DisplayName("Al transicionar a EN_TRASLADO se notifica al donante y a la entidad beneficiaria")
    void notificaInicioDeRuta() {
        Integer idDonacion = donacion.getId();
        Integer idSegmento = donacion.getDonacionesSegmentadas().get(0).getId();

        trazabilidadService.transicionEnTraslado(idDonacion, idSegmento, "Chofer García");

        // Verificar que se notificó al donante
        verify(notificacionRestClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("juan@mail.com"),
                argThat(s -> s.contains("ya inició la ruta de entrega")),
                eq("Tu donación está en camino"),
                eq(donante.getPersona().getId())
        );

        // Verificar que se notificó a la entidad
        verify(notificacionRestClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("comedor@mail.com"),
                argThat(s -> s.contains("en traslado hacia tu ubicación")),
                eq("Una donación está en camino hacia tu entidad"),
                eq(entidad.getDatosDeEntidad().getId())
        );
    }

    @Test
    @DisplayName("Al transicionar a ENTREGADA se notifica al donante y a la entidad beneficiaria")
    void notificaEntregaExitosa() {
        Integer idDonacion = donacion.getId();
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        Integer idSegmento = segmento.getId();

        // Primero pasar a EN_TRASLADO
        segmento.iniciarTraslado("Chofer");
        reset(notificacionRestClient); // Limpiar invocaciones previas

        trazabilidadService.transicionEntregaExitosa(idDonacion, idSegmento, "EntidadBeneficiaria");

        // Verificar que se notificó al donante
        verify(notificacionRestClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("juan@mail.com"),
                argThat(s -> s.contains("entregada exitosamente")),
                eq("¡Tu donación fue entregada con éxito!"),
                eq(donante.getPersona().getId())
        );

        // Verificar que se notificó a la entidad
        verify(notificacionRestClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("comedor@mail.com"),
                argThat(s -> s.contains("entregada y confirmada")),
                eq("Entrega recibida exitosamente"),
                eq(entidad.getDatosDeEntidad().getId())
        );
    }

    @Test
    @DisplayName("Al transicionar a ENTREGA_FALLIDA se notifica al donante y a la entidad con justificación")
    void notificaEntregaNoSatisfactoria() {
        Integer idDonacion = donacion.getId();
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        Integer idSegmento = segmento.getId();

        // Primero pasar a EN_TRASLADO
        segmento.iniciarTraslado("Chofer");
        reset(notificacionRestClient);

        String justificacion = "Tocamos timbre pero nadie respondió";
        trazabilidadService.transicionEntregaFallida(idDonacion, idSegmento, "Chofer", justificacion);

        // Verificar que se notificó al donante con la justificación
        verify(notificacionRestClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("juan@mail.com"),
                argThat(s -> s.contains("Tocamos timbre pero nadie respondió")),
                eq("Hubo un problema con la entrega de tu donación"),
                eq(donante.getPersona().getId())
        );

        // Verificar que se notificó a la entidad con la justificación
        verify(notificacionRestClient).notificar(
                eq(TipoNotificador.EMAIL),
                eq("comedor@mail.com"),
                argThat(s -> s.contains("Tocamos timbre pero nadie respondió")),
                eq("La entrega no pudo completarse"),
                eq(entidad.getDatosDeEntidad().getId())
        );
    }

    @Test
    @DisplayName("Si falla el envío de notificación, la transición de estado no se interrumpe")
    void falloEnNotificacionNoInterrumpeTransicion() {
        Integer idDonacion = donacion.getId();
        Integer idSegmento = donacion.getDonacionesSegmentadas().get(0).getId();

        // Simular fallo en el envío de notificación
        when(notificacionRestClient.notificar(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Servicio de notificaciones no disponible"));

        // No debe lanzar excepción
        assertDoesNotThrow(() ->
                trazabilidadService.transicionEnTraslado(idDonacion, idSegmento, "Chofer")
        );

        // Verificar que la transición de estado sí se realizó
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        assertEquals(EstadoDonacionSegmentada.EN_TRASLADO, segmento.getEstado());
    }

    @Test
    @DisplayName("Si la donación no tiene donante, no se intenta notificar al donante")
    void sinDonanteNoNotifica() {
        // Crear donación sin donante
        SubCategoria sub = new SubCategoria(CategoriaBien.ALIMENTOS, "Arroz", Unidad.KILOGRAMOS);
        BienPerecedero bien = new BienPerecedero("Arroz", "Arroz 1kg", null, sub, new Date());
        Donacion donacionSinDonante = new Donacion(null, "Donación anónima", new Date(), List.of(bien));
        donacionRepository.save(donacionSinDonante);

        DonacionSegmentada segmento = donacionSinDonante.getDonacionesSegmentadas().get(0);
        segmento.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");
        segmento.listarParaEntrega("Logística");

        Integer idDonacion = donacionSinDonante.getId();
        Integer idSegmento = segmento.getId();

        assertDoesNotThrow(() ->
                trazabilidadService.transicionEnTraslado(idDonacion, idSegmento, "Chofer")
        );

        // No se notifica a nadie (no hay donante ni entidad asignada)
        verify(notificacionRestClient, never()).notificar(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Si el donante no tiene contacto predeterminado, no se intenta enviar notificación")
    void sinContactoPredeterminadoNoNotifica() {
        // Crear donante sin medio predeterminado
        PersonaHumana personaSinContacto = new PersonaHumana();
        personaSinContacto.setNombre("María");
        personaSinContacto.setApellido("López");
        // No se setea medioPredeterminado → getContactoPredeterminado() retorna null
        Donante donanteSinContacto = new Donante(personaSinContacto);
        donanteRepository.create(donanteSinContacto);

        SubCategoria sub = new SubCategoria(CategoriaBien.VESTIMENTA, "Camperas", Unidad.UNIDADES);
        BienDuradero bien = new BienDuradero("Campera", "Campera de abrigo", null, sub, EstadoBien.NUEVO);
        Donacion donacionNueva = new Donacion(donanteSinContacto, "Camperas", new Date(), List.of(bien));
        donacionRepository.save(donacionNueva);

        DonacionSegmentada segmento = donacionNueva.getDonacionesSegmentadas().get(0);
        segmento.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");
        segmento.listarParaEntrega("Logística");

        Integer idDonacion = donacionNueva.getId();
        Integer idSegmento = segmento.getId();

        assertDoesNotThrow(() ->
                trazabilidadService.transicionEnTraslado(idDonacion, idSegmento, "Chofer")
        );

        // No se invoca al servicio de notificaciones
        verify(notificacionRestClient, never()).notificar(any(), any(), any(), any(), any());
    }
}
