package com.tp.donatrack.notificaciones.domain.notificadores.email.providers;

import com.tp.donatrack.notificaciones.domain.notificadores.email.iEmailProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación real del proveedor de email usando la API de Resend.
 * Envía emails transaccionales vía HTTP POST a la API de Resend.
 * Documentación: https://resend.com/docs/api-reference/emails/send-email
 */
@Component
@ConditionalOnProperty(name = "resend.api.key", matchIfMissing = false)
public class Resend implements iEmailProvider {

    private static final Logger logger = LoggerFactory.getLogger(Resend.class);

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private static final String API_URL = "https://api.resend.com/emails";

    @Override
    public void enviarEmail(String destinatario, String mensaje, String asunto) {
        logger.info("--- ENVIANDO EMAIL REAL A {} ---", destinatario);
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", fromEmail);
            body.put("to", new String[]{destinatario});
            body.put("subject", asunto);
            body.put("text", mensaje);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Email enviado exitosamente a {}. Respuesta: {}", destinatario, response.getBody());
            } else {
                logger.warn("Resend respondió con código no exitoso: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error al enviar email a {}: {}", destinatario, e.getMessage(), e);
        }
    }
}
