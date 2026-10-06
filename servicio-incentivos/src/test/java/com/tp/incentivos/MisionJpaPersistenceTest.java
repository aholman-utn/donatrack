package com.tp.incentivos;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.incentivos.domain.misiones.*;
import com.tp.incentivos.repositories.MisionRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(MisionJpaPersistenceTest.TestConfig.class)
public class MisionJpaPersistenceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ConnectionFactory connectionFactory() {
            return new CachingConnectionFactory();
        }
    }

    @Autowired
    private MisionRepository misionRepository;

    @Test
    @DisplayName("Persistir jerarquía polimórfica de misiones en tabla única")
    public void testPersistirMisionesPolimorficas() {
        Mision colExitosas = new MisionDonacionesExitosas(
                1,
                "Donacion Exitosa",
                "Lograr 1 donación que sea recibida exitosamente."
        );
        colExitosas.setNivel(Nivel.COLABORADOR);
        colExitosas.setOrden(1);

        Mision colHabil = new MisionHabilDonador(
                5,
                "Habil Donador",
                "Realizar una donación que supere los 5 bienes."
        );
        colHabil.setNivel(Nivel.COLABORADOR);
        colHabil.setOrden(2);

        Mision traRacha = new MisionRacha(
                3,
                "Racha",
                "Realizar una donación durante 3 meses consecutivos."
        );
        traRacha.setNivel(Nivel.TRANSFORMADOR);
        traRacha.setOrden(1);

        misionRepository.saveAll(List.of(colExitosas, colHabil, traRacha));

        assertEquals(3, misionRepository.count());

        Optional<Mision> buscada = misionRepository.findById(Nivel.COLABORADOR, 1L);
        assertTrue(buscada.isPresent());
        assertTrue(buscada.get() instanceof MisionDonacionesExitosas);
        assertEquals("Donacion Exitosa", buscada.get().getTitulo());
        assertNotNull(buscada.get().getInsigniaAsociada());

        Optional<Mision> siguiente = misionRepository.findSiguiente(Nivel.COLABORADOR, 1L);
        assertTrue(siguiente.isPresent());
        assertEquals(2, siguiente.get().getOrden());
        assertTrue(siguiente.get() instanceof MisionHabilDonador);

        Optional<Mision> transformadorMision = misionRepository.findById(Nivel.TRANSFORMADOR, 1L);
        assertTrue(transformadorMision.isPresent());
        assertTrue(transformadorMision.get() instanceof MisionRacha);
    }
}
