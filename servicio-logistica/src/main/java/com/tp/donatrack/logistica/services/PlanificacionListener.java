package com.tp.donatrack.logistica.services;

import com.tp.commons.config.RabbitMQCommonsConfig;
import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlanificacionListener {

    private static final Logger logger = LoggerFactory.getLogger(PlanificacionListener.class);
    private final RutaService rutaService;

    public PlanificacionListener(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @RabbitListener(queues = RabbitMQCommonsConfig.COLA_PLANIFICAR)
    public void recibirLotePlanificacion(List<DonacionSegmentadaListaParaEntregarALogisticaDTO> loteDonaciones) {
        logger.info("Recibido lote de {} donaciones para planificación desde RabbitMQ.", loteDonaciones.size());
        try {
            rutaService.planificarLote(loteDonaciones);
            logger.info("Planificación de lote completada exitosamente.");
        } catch (Exception e) {
            // El lote se consume igual: sin DLQ configurada, propagar la excepción
            // haría que RabbitMQ lo re-encole indefinidamente (ej: no hay camiones registrados).
            logger.error("Error crítico al planificar el lote recibido: {}", e.getMessage(), e);
        }
    }
}
