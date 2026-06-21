package com.tp.commons.services.notificador;

import  com.tp.commons.domain.notificador.TipoNotificador;
import  com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificacionRestClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(NotificacionRestClient.class);

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
            String urlServicioNotificaciones = "http://localhost:8082/api/notificaciones/notificar";
            restTemplate.postForEntity(urlServicioNotificaciones, dto, Void.class);
            logger.info("Notificación enviada exitosamente para la persona ID: {}", personaId);
            return true;
        } catch (Exception e) {
            // Acá registrás el error para tener trazabilidad
            logger.error("Error al comunicar con servicio de notificaciones: {}", e.getMessage());
            return false;
        }
    }
}