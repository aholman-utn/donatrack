package com.tp.donatrack.services;

import com.tp.donatrack.domain.notificador.TipoNotificador;
import com.tp.donatrack.dtos.NotificacionRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificacionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String urlServicioNotificaciones = "http://localhost:8082/api/notificaciones/notificar";
    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);

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