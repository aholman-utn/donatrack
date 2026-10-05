package com.tp.donatrack.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.commons.domain.donaciones.Unidad;
import com.tp.commons.dtos.logistica.EventoLogisticaDTO;
import com.tp.commons.dtos.logistica.TipoEventoLogistica;
import com.tp.donatrack.clients.LogisticaQueueClient;
import com.tp.donatrack.domain.bien.*;
import com.tp.donatrack.domain.donacion.*;
import com.tp.donatrack.domain.donacion.exception.TransicionNoPermitidaException;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.tasks.DonacionesListasParaEntregarCron;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Integrates the real State objects, repository, cron and event listener.
 * Transport and notifications are mocked: no external messages are sent. */
class LogisticaStateIntegrationTest {
    private DonacionRepository repository;
    private Donacion donacion;
    private DonacionSegmentada segmento;
    private PersonaJuridica persona;
    private EntidadBeneficiariaService entidades;
    private LogisticaQueueClient queue;
    private TrazabilidadService trazabilidad;
    private DonacionesListasParaEntregarCron cron;
    private LogisticaEventListener listener;

    @BeforeEach
    void setUp() {
        repository = mock(DonacionRepository.class);
        when(repository.save(any(Donacion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var sub = new SubCategoria(CategoriaBien.MOBILIARIO, "Sillas", Unidad.UNIDADES);
        var bien = new BienDuradero("Silla", "Prueba", null, sub, EstadoBien.NUEVO);
        donacion = new Donacion(null, "Prueba", new Date(), List.of(bien));
        donacion.setId(1L);
        donacion.getDonacionesSegmentadas().getFirst().setId(1L);
        donacion = repository.save(donacion);
        segmento = donacion.getDonacionesSegmentadas().getFirst();
        when(repository.findAll()).thenReturn(List.of(donacion));
        // findSegmentadaById / findDonacionByDonacionesSegmentadaId son métodos
        // default de la interfaz; Mockito no los ejecuta, por lo que se stubean.
        when(repository.findSegmentadaById(segmento.getId())).thenReturn(segmento);
        when(repository.findDonacionByDonacionesSegmentadaId(segmento.getId())).thenReturn(donacion);
        segmento.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Test", "Asignada");
        segmento.listarParaEntrega("Test");
        segmento.setEntidadBeneficiariaAsignadaId(10L);
        persona = new PersonaJuridica();
        persona.setId(10L);
        var direccion = new Direccion();
        direccion.setCalle1("Calle de prueba");
        direccion.setAltura(123);
        persona.setDireccion(direccion);
        entidades = mock(EntidadBeneficiariaService.class);
        when(entidades.listarPorIds(any())).thenReturn(List.of(new EntidadBeneficiaria(persona)));
        queue = mock(LogisticaQueueClient.class);
        trazabilidad = mock(TrazabilidadService.class);
        var service = new DonacionService(repository, mock(DonanteService.class), entidades,
                mock(DonacionEventPublisher.class));
        cron = new DonacionesListasParaEntregarCron(service, entidades, queue);
        listener = new LogisticaEventListener(repository, trazabilidad);
    }

    @Test
    void dosCiclosPublicanUnaSolaVezYMantienenLaDonacionAdjudicada() {
        cron.enviarDonacionesListasParaEntregar();
        cron.enviarDonacionesListasParaEntregar();
        verify(queue, times(1)).enviarLoteDonaciones(argThat(lote -> lote.size() == 1));
        assertEquals(EstadoDonacionSegmentada.EN_PLANIFICACION, segmento.getEstado());
        assertEquals(EstadoDonacion.ADJUDICADA, donacion.getEstado());
        assertFalse(segmento.getEstado().isListaParaEntregar());
    }

    @Test
    void falloAlPublicarPermiteReintentarEnElSiguienteCiclo() {
        doThrow(new RuntimeException("Broker caido")).doNothing().when(queue).enviarLoteDonaciones(anyList());
        cron.enviarDonacionesListasParaEntregar();
        assertEquals(EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR, segmento.getEstado());
        cron.enviarDonacionesListasParaEntregar();
        assertEquals(EstadoDonacionSegmentada.EN_PLANIFICACION, segmento.getEstado());
        verify(queue, times(2)).enviarLoteDonaciones(anyList());
    }

    @Test
    void sinDireccionNoPublicaNiCambiaEstado() {
        persona.setDireccion(null);
        cron.enviarDonacionesListasParaEntregar();
        verifyNoInteractions(queue);
        assertEquals(EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR, segmento.getEstado());
    }

    @Test
    void direccionVaciaNoPublicaNiCambiaEstado() {
        persona.getDireccion().setCalle1("   ");
        cron.enviarDonacionesListasParaEntregar();
        verifyNoInteractions(queue);
        assertEquals(EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR, segmento.getEstado());
    }

    @Test
    void noPermiteSaltarPlanificacionNiSolicitarlaDosVeces() {
        assertTrue(segmento.transicionPosible(segmento.getEstado(), EstadoDonacionSegmentada.EN_PLANIFICACION));
        assertFalse(segmento.transicionPosible(segmento.getEstado(), EstadoDonacionSegmentada.EN_TRASLADO));
        assertThrows(TransicionNoPermitidaException.class, () -> segmento.iniciarTraslado("Test"));
        segmento.solicitarPlanificacion("Test");
        int eventos = segmento.getHistorial().size();
        assertThrows(TransicionNoPermitidaException.class, () -> segmento.solicitarPlanificacion("Test"));
        assertEquals(eventos, segmento.getHistorial().size());
    }

    @Test
    void listenerReconoceInstanciasStateYNoDuplicaInicioDeRuta() {
        segmento.solicitarPlanificacion("Test");
        assertNotSame(EstadoDonacionSegmentada.EN_PLANIFICACION, segmento.getEstado());
        listener.recibirEventoLogistica(evento(TipoEventoLogistica.INICIO_RUTA));
        int eventos = segmento.getHistorial().size();
        listener.recibirEventoLogistica(evento(TipoEventoLogistica.INICIO_RUTA));
        assertEquals(EstadoDonacionSegmentada.EN_TRASLADO, segmento.getEstado());
        assertEquals(eventos, segmento.getHistorial().size());
        verify(trazabilidad, times(1)).notificarInicioDeRuta(segmento);
    }

    @Test
    void listenerProcesaEntregaFallidaYRetornoADeposito() {
        segmento.solicitarPlanificacion("Test");
        segmento.iniciarTraslado("Test");
        listener.recibirEventoLogistica(evento(TipoEventoLogistica.ENTREGA_FALLIDA));
        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, segmento.getEstado());
        verify(trazabilidad).notificarEntregaNoSatisfactoria(segmento, "Prueba");
    }

    @Test
    void llegadaConservaTrasladoYEntregaInvocaRecepcion() {
        segmento.solicitarPlanificacion("Test");
        segmento.iniciarTraslado("Test");
        listener.recibirEventoLogistica(evento(TipoEventoLogistica.LLEGADA_A_DESTINO));
        assertEquals(EstadoDonacionSegmentada.EN_TRASLADO, segmento.getEstado());
        var entrega = evento(TipoEventoLogistica.ENTREGA_EXITOSA);
        listener.recibirEventoLogistica(entrega);
        verify(trazabilidad).recepcionarEntrega(donacion.getId(), segmento.getId(),
                entrega.getTimestamp(), "Prueba");
    }

    @Test
    void planificacionSeSerializaYDeserializa() throws Exception {
        segmento.solicitarPlanificacion("Test");
        var mapper = new ObjectMapper();
        assertEquals("\"EN_PLANIFICACION\"", mapper.writeValueAsString(segmento.getEstado()));
        assertEquals(segmento.getEstado(), mapper.readValue("\"EN_PLANIFICACION\"", EstadoDonacionSegmentada.class));
    }

    private EventoLogisticaDTO evento(TipoEventoLogistica tipo) {
        return new EventoLogisticaDTO(tipo, segmento.getId(), 10L, LocalDateTime.now(), "Prueba");
    }
}
