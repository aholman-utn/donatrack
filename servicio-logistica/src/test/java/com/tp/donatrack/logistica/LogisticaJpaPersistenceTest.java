package com.tp.donatrack.logistica;

import com.tp.donatrack.logistica.domain.*;
import com.tp.donatrack.logistica.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(LogisticaJpaPersistenceTest.TestConfig.class)
public class LogisticaJpaPersistenceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ConnectionFactory connectionFactory() {
            return new CachingConnectionFactory();
        }
    }

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private ChoferRepository choferRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private LogisticaEventRepository eventRepository;

    @Test
    @DisplayName("Persistir Camion y Chofer correctamente")
    public void testPersistirCamionYChofer() {
        Camion camion = Camion.builder()
                .patente("AB123CD")
                .marca("Mercedes")
                .modelo("Accelo")
                .capacidadCarga(5000.0)
                .volumen(25.0)
                .altura(2.8)
                .build();
        Camion savedCamion = camionRepository.save(camion);
        assertNotNull(savedCamion.getId());

        Chofer chofer = Chofer.builder()
                .nombre("Carlos")
                .apellido("Gomez")
                .dni("30123456")
                .build();
        Chofer savedChofer = choferRepository.save(chofer);
        assertNotNull(savedChofer.getId());

        assertTrue(camionRepository.findByPatente("AB123CD").isPresent());
        assertTrue(choferRepository.findByDni("30123456").isPresent());
    }

    @Test
    @DisplayName("Persistir Ruta con Paradas y Envíos en cascada")
    public void testPersistirRutaCompleta() {
        Camion camion = camionRepository.save(Camion.builder()
                .patente("AA999ZZ")
                .marca("Iveco")
                .modelo("Daily")
                .capacidadCarga(3500.0)
                .volumen(18.0)
                .altura(2.5)
                .build());

        Chofer chofer = choferRepository.save(Chofer.builder()
                .nombre("Mariano")
                .apellido("Lopez")
                .dni("35987654")
                .build());

        Ruta ruta = Ruta.builder()
                .camion(camion)
                .chofer(chofer)
                .iniciada(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Parada parada = Parada.builder()
                .orden(1)
                .direccion("Av. Corrientes 1234, CABA")
                .build();
        ruta.agregarParada(parada);

        Envio envio = Envio.builder()
                .donacionSegmentadaId(101L)
                .entidadBeneficiariaId(5L)
                .estado(EstadoEnvio.PENDIENTE)
                .build();
        parada.agregarEnvio(envio);

        Ruta rutaGuardada = rutaRepository.save(ruta);
        assertNotNull(rutaGuardada.getId());
        assertEquals(1, rutaGuardada.getParadas().size());

        List<Envio> envios = envioRepository.findAll();
        assertFalse(envios.isEmpty());
        assertEquals(101L, envios.get(0).getDonacionSegmentadaId());
    }

    @Test
    @DisplayName("Registrar Evento de Logística")
    public void testRegistrarEvento() {
        EventoLogistica evento = EventoLogistica.builder()
                .tipoEvento("INICIO_RUTA")
                .donacionSegmentadaId(200L)
                .entidadBeneficiariaId(10L)
                .timestamp(LocalDateTime.now())
                .detalles("Salida de depósito con camión AA999ZZ")
                .build();

        eventRepository.registrar(evento);
        List<EventoLogistica> eventos = eventRepository.obtenerTodos();
        assertEquals(1, eventos.size());
        assertEquals("INICIO_RUTA", eventos.get(0).getTipoEvento());
    }
}
