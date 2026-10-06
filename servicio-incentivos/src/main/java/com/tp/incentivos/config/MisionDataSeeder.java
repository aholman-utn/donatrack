package com.tp.incentivos.config;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.incentivos.domain.misiones.*;
import com.tp.incentivos.repositories.MisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Inicializador de datos de catálogo para el Servicio de Incentivos.
 * Carga las 10 misiones por defecto si la tabla 'misiones' se encuentra vacía.
 */
@Component
public class MisionDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MisionDataSeeder.class);
    private final MisionRepository misionRepository;

    public MisionDataSeeder(MisionRepository misionRepository) {
        this.misionRepository = misionRepository;
    }

    @Override
    public void run(String... args) {
        if (misionRepository.count() > 0) {
            logger.info("Catálogo de misiones ya inicializado ({} misiones existentes).", misionRepository.count());
            return;
        }

        logger.info("Inicializando catálogo de misiones en la base de datos...");

        // --- COLABORADOR ---
        Mision colExitosas = new MisionDonacionesExitosas(
                1,
                "Donacion Exitosa",
                "Lograr 1 donación que sea recibida exitosamente por una entidad beneficiaria."
        );
        colExitosas.setNivel(Nivel.COLABORADOR);
        colExitosas.setOrden(1);

        Mision colHabil = new MisionHabilDonador(
                5,
                "Habil Donador",
                "Realizar una donación que supere los 5 bienes en una sola entrega."
        );
        colHabil.setNivel(Nivel.COLABORADOR);
        colHabil.setOrden(2);

        Mision colCompletitud = new MisionCompletitud(
                2,
                "Completitud",
                "Realizar donaciones de 2 categorías distintas."
        );
        colCompletitud.setNivel(Nivel.COLABORADOR);
        colCompletitud.setOrden(3);

        // --- SOSTENEDOR ---
        Mision sosExitosas = new MisionDonacionesExitosas(
                5,
                "Donacion Exitosa",
                "Lograr 5 donaciones que sean recibidas exitosamente por una entidad beneficiaria."
        );
        sosExitosas.setNivel(Nivel.SOSTENEDOR);
        sosExitosas.setOrden(1);

        Mision sosCompletitud = new MisionCompletitud(
                4,
                "Completitud",
                "Realizar donaciones de 4 categorías distintas."
        );
        sosCompletitud.setNivel(Nivel.SOSTENEDOR);
        sosCompletitud.setOrden(2);

        Mision sosHabil = new MisionHabilDonador(
                10,
                "Habil Donador",
                "Realizar una donación que supere los 10 bienes en una sola entrega."
        );
        sosHabil.setNivel(Nivel.SOSTENEDOR);
        sosHabil.setOrden(3);

        // --- TRANSFORMADOR ---
        Mision traRacha = new MisionRacha(
                3,
                "Racha",
                "Realizar una donación durante 3 meses consecutivos."
        );
        traRacha.setNivel(Nivel.TRANSFORMADOR);
        traRacha.setOrden(1);

        Mision traExitosas = new MisionDonacionesExitosas(
                15,
                "Donacion Exitosa",
                "Lograr 15 donaciones que sean recibidas exitosamente por una entidad beneficiaria."
        );
        traExitosas.setNivel(Nivel.TRANSFORMADOR);
        traExitosas.setOrden(2);

        Mision traCompletitud = new MisionCompletitud(
                5,
                "Completitud",
                "Realizar donaciones de 5 categorías distintas."
        );
        traCompletitud.setNivel(Nivel.TRANSFORMADOR);
        traCompletitud.setOrden(3);

        Mision traHabil = new MisionHabilDonador(
                20,
                "Habil Donador",
                "Realizar una donación que supere los 20 bienes en una sola entrega."
        );
        traHabil.setNivel(Nivel.TRANSFORMADOR);
        traHabil.setOrden(4);

        misionRepository.saveAll(List.of(
                colExitosas, colHabil, colCompletitud,
                sosExitosas, sosCompletitud, sosHabil,
                traRacha, traExitosas, traCompletitud, traHabil
        ));

        logger.info("Catálogo de misiones inicializado exitosamente (10 misiones guardadas).");
    }
}
