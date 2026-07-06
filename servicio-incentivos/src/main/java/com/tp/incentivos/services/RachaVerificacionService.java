package com.tp.incentivos.services;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.commons.dtos.incentivos.DonanteRachaDTO;
import com.tp.incentivos.clients.DonacionesRestClient;
import com.tp.incentivos.domain.misiones.Mision;
import com.tp.incentivos.domain.misiones.MisionRacha;
import com.tp.incentivos.repositories.MisionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class RachaVerificacionService {

    private final DonacionesRestClient donacionesRestClient;
    private final MisionRepository misionRepository;
    private final IncentivosService incentivosService;
    private static final Logger logger = LoggerFactory.getLogger(RachaVerificacionService.class);

    private static final int DIAS_LIMITE_RACHA = 30;
    private static final int DIAS_ADVERTENCIA_RACHA = 5;

    public RachaVerificacionService(
            DonacionesRestClient donacionesRestClient,
            MisionRepository misionRepository,
            IncentivosService incentivosService) {
        this.donacionesRestClient = donacionesRestClient;
        this.misionRepository = misionRepository;
        this.incentivosService = incentivosService;
    }

    // Se ejecuta todos los días a las 00:00
    @Scheduled(cron = "0 0 0 * * *")
    public void verificarRachasDonantes() {
        logger.info("Iniciando verificación diaria de rachas...");

        try {
            List<DonanteRachaDTO> donantes = donacionesRestClient.obtenerDonantesConMisionActual();
            logger.info("Se encontraron {} donantes para verificar", donantes.size());

            for (DonanteRachaDTO donante : donantes) {
                try {
                    verificarRachaDonante(donante);
                } catch (Exception e) {
                    logger.error("Error al verificar racha del donante {}: {}",
                            donante.getDonanteId(), e.getMessage());
                }
            }

            logger.info("Verificación diaria de rachas finalizada.");
        } catch (Exception e) {
            logger.error("Error al obtener la lista de donantes para verificar rachas: {}", e.getMessage());
        }
    }

    private void verificarRachaDonante(DonanteRachaDTO donante) {
        Nivel nivel = donante.getNivel();
        Long misionActualId = donante.getMisionActualId();

        // Verificar si la misión actual es MisionRacha
        Optional<Mision> misionOpt = misionRepository.findById(nivel, misionActualId);

        if (misionOpt.isEmpty() || !(misionOpt.get() instanceof MisionRacha)) {
            return; // No es una misión de racha, no hay nada que verificar
        }

        // Es MisionRacha, obtener la fecha de la última donación
        LocalDate fechaUltimaDonacion = donacionesRestClient.obtenerFechaUltimaDonacion(donante.getDonanteId());

        if (fechaUltimaDonacion == null) {
            // Sin donaciones, la racha ya está en 0
            return;
        }

        long diasSinDonar = ChronoUnit.DAYS.between(fechaUltimaDonacion, LocalDate.now());

        if (diasSinDonar >= DIAS_LIMITE_RACHA) {
            // La racha se perdió
            logger.info("Donante {} perdió la racha. Última donación hace {} días.",
                    donante.getDonanteId(), diasSinDonar);

            donacionesRestClient.resetearProgresoRacha(donante.getDonanteId());

            incentivosService.notificarRachaPerdida(donante.getDonanteId());

        } else if (diasSinDonar >= (DIAS_LIMITE_RACHA - DIAS_ADVERTENCIA_RACHA)) {
            // Faltan 5 días o menos para perder la racha
            long diasRestantes = DIAS_LIMITE_RACHA - diasSinDonar;
            logger.info("Donante {} está por perder la racha. Le quedan {} días.",
                    donante.getDonanteId(), diasRestantes);

            incentivosService.notificarAdvertenciaRacha(donante.getDonanteId(), diasRestantes);
        }
    }
}
