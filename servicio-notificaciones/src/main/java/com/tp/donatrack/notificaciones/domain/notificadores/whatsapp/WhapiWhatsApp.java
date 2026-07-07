package com.tp.donatrack.notificaciones.domain.notificadores.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WhapiWhatsApp implements iWhatsAppProvider {
    private static final Logger logger = LoggerFactory.getLogger(WhapiWhatsApp.class);

    @Value("${whapi.api.token}")
    private String apiToken;

    private static final String API_URL = "https://gate.whapi.cloud/messages/text";

    @Override
    public void enviarWhatsApp(String numero, String mensaje) {
        logger.info("--- ENVIANDO WHATSAPP REAL A {} ---", numero);
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            headers.set("Accept", "application/json");

            Map<String, String> body = new HashMap<>();
            body.put("to", numero);
            body.put("body", mensaje);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("¡Mensaje entregado! Respuesta de Whapi: {}", response.getBody());
            } else {
                logger.warn("La API respondió con código no exitoso: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Explotó el envío del mensaje a {}: {}", numero, e.getMessage(), e);
        }
    }
}