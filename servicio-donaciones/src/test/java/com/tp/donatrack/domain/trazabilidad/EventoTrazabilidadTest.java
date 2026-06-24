package com.tp.donatrack.domain.trazabilidad;

import com.tp.donatrack.domain.bien.*;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventoTrazabilidadTest {

    private DonacionSegmentada donacion;
    private SubCategoria subCategoria;

    @BeforeEach
    void setUp() {
        subCategoria = new SubCategoria(CategoriaBien.MOBILIARIO, "Sillas", Unidad.UNIDADES);
        BienDuradero silla = new BienDuradero("Silla", "Silla de oficina", "silla.png", subCategoria, EstadoBien.USADO);
        List<Bien> bienes = Arrays.asList(silla);
        donacion = new DonacionSegmentada(1, subCategoria, bienes);
    }

    @Test
    @DisplayName("Al registrar una donación, queda EN_DEPOSITO con evento inicial")
    void registraEventoInicial() {
        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, donacion.getEstado());

        List<EventoTrazabilidad> historial = donacion.getHistorial();
        assertEquals(1, historial.size());

        EventoTrazabilidad evento = historial.get(0);
        assertNull(evento.getEstadoAnterior());
        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, evento.getEstadoNuevo());
        assertEquals("Administrador", evento.getActor());
    }

    @Test
    @DisplayName("Flujo completo: EN_DEPOSITO → ASIGNACION_REALIZADA → LISTA_PARA_ENTREGAR → EN_TRASLADO → ENTREGADA")
    void flujoCompletoExitoso() {
        donacion.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada por algoritmo");
        donacion.listarParaEntrega("Logística");
        donacion.iniciarTraslado("Chofer Pérez");
        donacion.confirmarEntrega(123L);

        assertEquals(EstadoDonacionSegmentada.ENTREGADA, donacion.getEstado());
        assertEquals(5, donacion.getHistorial().size());
    }

    @Test
    @DisplayName("Entrega fallida registra justificación y vuelve a EN_DEPOSITO")
    void entregaFallidaConJustificacion() {
        donacion.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");
        donacion.listarParaEntrega("Logística");
        donacion.iniciarTraslado("Chofer");

        donacion.registrarEntregaFallida("Chofer", "Tocamos timbre pero nadie respondió");

        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, donacion.getEstado());

        // Verificar que se registraron ambos eventos: ENTREGA_FALLIDA y vuelta a EN_DEPOSITO
        List<EventoTrazabilidad> historial = donacion.getHistorial();
        EventoTrazabilidad eventoFallido = historial.get(historial.size() - 2);
        assertEquals(EstadoDonacionSegmentada.ENTREGA_FALLIDA, eventoFallido.getEstadoNuevo());
        assertEquals("Tocamos timbre pero nadie respondió", eventoFallido.getDescripcion());

        EventoTrazabilidad eventoVuelta = historial.get(historial.size() - 1);
        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, eventoVuelta.getEstadoNuevo());
    }

    @Test
    @DisplayName("Administrador puede marcar una donación como vencida")
    void marcarVencida() {
        donacion.marcarVencida("Admin López");

        assertEquals(EstadoDonacionSegmentada.VENCIDA, donacion.getEstado());
        EventoTrazabilidad ultimo = donacion.getUltimoEvento();
        assertEquals("Admin López", ultimo.getActor());
        assertEquals(EstadoDonacionSegmentada.VENCIDA, ultimo.getEstadoNuevo());
    }

    @Test
    @DisplayName("El historial es inmutable desde fuera")
    void historialInmutable() {
        List<EventoTrazabilidad> historial = donacion.getHistorial();
        assertThrows(UnsupportedOperationException.class, () -> historial.clear());
    }

    @Test
    @DisplayName("getUltimoEvento devuelve el evento más reciente")
    void ultimoEvento() {
        donacion.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");
        donacion.listarParaEntrega("Logística");

        EventoTrazabilidad ultimo = donacion.getUltimoEvento();
        assertEquals(EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR, ultimo.getEstadoNuevo());
    }

    @Test
    @DisplayName("Cada evento tiene fecha no nula")
    void eventosConFecha() {
        donacion.transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, "Sistema", "Asignada");

        for (EventoTrazabilidad evento : donacion.getHistorial()) {
            assertNotNull(evento.getFecha());
        }
    }
}
