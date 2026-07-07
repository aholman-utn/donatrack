package com.tp.commons.services.notificador;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificacionQueueClient {
    private final RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(NotificacionQueueClient.class);

    private static final String QUEUE_NAME = "notificaciones.queue";

    public NotificacionQueueClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean notificar(
            TipoNotificador tipo,
            String destinatario,
            String mensaje,
            String asunto,
            Long personaId
    ) {
        NotificacionRequestDTO dto = new NotificacionRequestDTO();
        dto.setMedio(tipo);
        dto.setDestinatario(destinatario);
        dto.setMensaje(mensaje);
        dto.setAsunto(asunto);
        dto.setIdPersona(personaId);

        try {
            rabbitTemplate.convertAndSend(QUEUE_NAME, dto);

            logger.info("Notificación asíncrona encolada exitosamente para la persona ID: {}", personaId);
            return true;
        } catch (Exception e) {
            logger.error("Error crítico al intentar encolar la notificación en RabbitMQ: {}", e.getMessage(), e);
            return false;
        }
    }
}