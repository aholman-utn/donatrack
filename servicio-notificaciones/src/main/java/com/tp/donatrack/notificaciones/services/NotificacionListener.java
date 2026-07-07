package com.tp.donatrack.notificaciones.services;

import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacionListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionListener.class);
    private final NotificacionService notificacionService;

    public NotificacionListener(NotificacionService service) {
        this.notificacionService = service;
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "notificaciones.queue", durable = "true"))
    public void recibirNotificacion(NotificacionRequestDTO body) {
        try {
            this.notificacionService.notificar(body);
            logger.info("Notificación procesada exitosamente desde la cola para la persona ID: {}", body.getIdPersona());

        } catch (IllegalArgumentException e) {
            logger.error("Datos inválidos en el mensaje de la cola: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error interno al procesar notificación de la cola para ID {}: {}", body.getIdPersona(), e.getMessage());
            throw e;
        }
    }
}