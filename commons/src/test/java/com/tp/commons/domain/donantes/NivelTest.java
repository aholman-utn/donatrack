package com.tp.commons.domain.donantes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class NivelTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Colaborador debe avanzar a Sostenedor")
    public void testColaboradorAvanzaASostenedor() {
        Nivel nivel = Nivel.COLABORADOR;
        assertTrue(nivel.tieneSiguienteNivel());

        Optional<Nivel> siguiente = nivel.obtenerSiguienteNivel();
        assertTrue(siguiente.isPresent());
        assertEquals(Nivel.SOSTENEDOR, siguiente.get());
        assertEquals("SOSTENEDOR", siguiente.get().getNombre());
    }

    @Test
    @DisplayName("Sostenedor debe avanzar a Transformador")
    public void testSostenedorAvanzaATransformador() {
        Nivel nivel = Nivel.SOSTENEDOR;
        assertTrue(nivel.tieneSiguienteNivel());

        Optional<Nivel> siguiente = nivel.obtenerSiguienteNivel();
        assertTrue(siguiente.isPresent());
        assertEquals(Nivel.TRANSFORMADOR, siguiente.get());
        assertEquals("TRANSFORMADOR", siguiente.get().getNombre());
    }

    @Test
    @DisplayName("Transformador es el nivel máximo y no tiene siguiente")
    public void testTransformadorNoTieneSiguiente() {
        Nivel nivel = Nivel.TRANSFORMADOR;
        assertFalse(nivel.tieneSiguienteNivel());
        assertTrue(nivel.obtenerSiguienteNivel().isEmpty());
    }

    @Test
    @DisplayName("Serialización Jackson debe emitir el nombre del nivel como String simple")
    public void testSerializacionJackson() throws JsonProcessingException {
        String jsonColaborador = objectMapper.writeValueAsString(Nivel.COLABORADOR);
        assertEquals("\"COLABORADOR\"", jsonColaborador);

        String jsonSostenedor = objectMapper.writeValueAsString(Nivel.SOSTENEDOR);
        assertEquals("\"SOSTENEDOR\"", jsonSostenedor);

        String jsonTransformador = objectMapper.writeValueAsString(Nivel.TRANSFORMADOR);
        assertEquals("\"TRANSFORMADOR\"", jsonTransformador);
    }

    @Test
    @DisplayName("Deserialización Jackson debe convertir String al estado correspondiente")
    public void testDeserializacionJackson() throws JsonProcessingException {
        Nivel col = objectMapper.readValue("\"COLABORADOR\"", Nivel.class);
        assertEquals(Nivel.COLABORADOR, col);

        Nivel sos = objectMapper.readValue("\"sostenedor\"", Nivel.class);
        assertEquals(Nivel.SOSTENEDOR, sos);

        Nivel tra = objectMapper.readValue("\"TRANSFORMADOR\"", Nivel.class);
        assertEquals(Nivel.TRANSFORMADOR, tra);
    }

    @Test
    @DisplayName("equals y hashCode deben basarse en el nombre del nivel")
    public void testEqualsAndHashCode() {
        assertEquals(new Colaborador(), Nivel.COLABORADOR);
        assertEquals(new Sostenedor(), Nivel.SOSTENEDOR);
        assertEquals(new Transformador(), Nivel.TRANSFORMADOR);
        assertNotEquals(Nivel.COLABORADOR, Nivel.SOSTENEDOR);
        assertEquals(Nivel.COLABORADOR.hashCode(), new Colaborador().hashCode());
    }
}
