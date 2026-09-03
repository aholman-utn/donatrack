package com.tp.donatrack.logistica.clients;

import com.tp.commons.config.RabbitMQCommonsConfig;
import com.tp.commons.dtos.logistica.EventoLogisticaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class DonacionesQueueClient {
    private final RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DonacionesQueueClient.class);

    public DonacionesQueueClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEvento(EventoLogisticaDTO evento) {
        try {
            logger.info("Publicando evento logístico '{}' al exchange de eventos.", evento.getTipoEvento());
            // Se usa el exchange de topic y el routing key logistica.eventos.donaciones (matchea con logistica.eventos.#)
            rabbitTemplate.convertAndSend(RabbitMQCommonsConfig.LOGISTICA_EVENTOS_EXCHANGE, "logistica.eventos.donaciones", evento);
        } catch (Exception e) {
            logger.error("Error al publicar el evento logístico {} para la donación {}: {}",
                    evento.getTipoEvento(), evento.getDonacionSegmentadaId(), e.getMessage(), e);
        }
    }
}
