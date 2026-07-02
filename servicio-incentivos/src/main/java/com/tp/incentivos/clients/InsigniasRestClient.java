package com.tp.incentivos.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class InsigniasRestClient {

    private static final Logger logger = LoggerFactory.getLogger(InsigniasRestClient.class);
    private final RestTemplate restTemplate;
    private final String n8nWebhookUrl;

    public InsigniasRestClient(
            RestTemplate restTemplate,
            @Value("${n8n.webhook.url}") String n8nWebhookUrl) {
        this.restTemplate = restTemplate;
        this.n8nWebhookUrl = n8nWebhookUrl;
    }

    public void notificarInsigniaObtenida(
            String nombreDonante,
            String tituloMision,
            String descripcionMision
    ) {
        try {
            logger.info("Enviando notificación a n8n para el usuario: {}", nombreDonante);

            Map<String, String> body = new HashMap<>();
            body.put("user", nombreDonante);
            body.put("nombreMision", tituloMision);
            body.put("descripcion", descripcionMision);

            restTemplate.postForObject(this.n8nWebhookUrl, body, String.class);

            logger.info("Notificación enviada a n8n exitosamente.");
        } catch (Exception e) {
            logger.error("Error al notificar a n8n: {}", e.getMessage());
        }
    }
}