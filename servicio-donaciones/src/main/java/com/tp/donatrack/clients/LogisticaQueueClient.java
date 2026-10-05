package com.tp.donatrack.clients;

import com.tp.commons.config.RabbitMQCommonsConfig;
import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogisticaQueueClient {
    private final RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(LogisticaQueueClient.class);

    public LogisticaQueueClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarLoteDonaciones(List<DonacionSegmentadaListaParaEntregarALogisticaDTO> lote) {
        try {
            logger.info("Enviando lote de {} donaciones a la cola de planificación logística", lote.size());
            rabbitTemplate.convertAndSend(RabbitMQCommonsConfig.COLA_PLANIFICAR, lote);
        } catch (Exception e) {
            logger.error("Error crítico al intentar encolar el lote de donaciones para planificación: {}", e.getMessage(), e);
            throw new RuntimeException("Falló la comunicación con el broker para enviar el lote a logística: " + e.getMessage(), e);
        }
    }
}
