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
        subCategoria = new SubCategoria(Categoria.MOBILIARIO, "Sillas", Unidad.UNIDADES);
        BienDuradero silla = new BienDuradero("Silla", "Silla de oficina", "silla.png", subCategoria, EstadoBien.USADO);
        List<Bien> bienes = Arrays.asList(silla);
        donacion = new DonacionSegmentada(1, subCategoria, bienes);
    }

    @Test
    @DisplayName("Al crear una donación segmentada, se registra el evento inicial")
    void registraEventoInicial() {
        List<EventoTrazabilidad> historial = donacion.getHistorial();

        assertEquals(1, historial.size());
        EventoTrazabilidad evento = historial.get(0);
        assertNull(evento.getEstadoAnterior());
        assertEquals(EstadoDonacionSegmentada.PENDIENTE, evento.getEstadoNuevo());
        assertEquals("Sistema", evento.getActor());
        assertNotNull(evento.getFecha());
    }

    @Test
    @DisplayName("Al transicionar estado, se registra el evento con actor y descripción")
    void registraEventoAlTransicionar() {
        donacion.transicionar(EstadoDonacionSegmentada.EN_DEPOSITO, "Administrador", "Ingresada al depósito");

        List<EventoTrazabilidad> historial = donacion.getHistorial();
        assertEquals(2, historial.size());

        EventoTrazabilidad evento = historial.get(1);
        assertEquals(EstadoDonacionSegmentada.PENDIENTE, evento.getEstadoAnterior());
        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, evento.getEstadoNuevo());
        assertEquals("Administrador", evento.getActor());
        assertEquals("Ingresada al depósito", evento.getDescripcion());
    }

    @Test
    @DisplayName("El historial registra toda la cadena de transiciones")
    void registraCadenaCompleta() {
        donacion.transicionar(EstadoDonacionSegmentada.EN_DEPOSITO, "Administrador", "Ingresada al depósito");
        donacion.transicionar(EstadoDonacionSegmentada.ADJUDICADA, "Administrador", "Asignada a Escuela N°10");
        donacion.marcarEnTransito("Logística");
        donacion.confirmarEntrega("Escuela N°10");

        List<EventoTrazabilidad> historial = donacion.getHistorial();
        assertEquals(5, historial.size());
        assertEquals(EstadoDonacionSegmentada.ENTREGADA, donacion.getEstado());
        assertEquals(EstadoDonacionSegmentada.ENTREGADA, donacion.getUltimoEvento().getEstadoNuevo());
    }

    @Test
    @DisplayName("marcarVencida registra el evento correctamente")
    void marcarVencidaRegistraEvento() {
        donacion.marcarVencida();

        assertEquals(EstadoDonacionSegmentada.VENCIDA, donacion.getEstado());
        EventoTrazabilidad ultimo = donacion.getUltimoEvento();
        assertEquals("Sistema", ultimo.getActor());
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
    void ultimoEventoEsElMasReciente() {
        donacion.transicionar(EstadoDonacionSegmentada.EN_DEPOSITO, "Admin", "Depósito");
        donacion.transicionar(EstadoDonacionSegmentada.ADJUDICADA, "Admin", "Asignada");

        EventoTrazabilidad ultimo = donacion.getUltimoEvento();
        assertEquals(EstadoDonacionSegmentada.ADJUDICADA, ultimo.getEstadoNuevo());
        assertEquals("Asignada", ultimo.getDescripcion());
    }
}
