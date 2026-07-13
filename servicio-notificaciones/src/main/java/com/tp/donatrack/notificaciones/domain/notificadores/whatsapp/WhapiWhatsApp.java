package com.tp.donatrack.notificaciones.domain.notificadores.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WhapiWhatsApp implements iWhatsAppProvider {
    private static final Logger logger = LoggerFactory.getLogger(WhapiWhatsApp.class);

    @Value("${whapi.api.token}")
    private String apiToken;

    private static final String API_URL_TEXT = "https://gate.whapi.cloud/messages/text";
    private static final String API_URL_DOCUMENT = "https://gate.whapi.cloud/messages/document";

    private final RestTemplate restTemplate;

    public WhapiWhatsApp() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void enviarWhatsApp(String numero, String mensaje, String asunto) {
        logger.info("--- ENVIANDO WHATSAPP A {} ---", numero);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            headers.set("Accept", "application/json");

            Map<String, String> body = new HashMap<>();

            String numeroLimpio = numero.replace("+", "").replace(" ", "");
            body.put("to", numeroLimpio);
            String mensajeFinal = mensaje;
            if (asunto != null && !asunto.trim().isEmpty()) {
                mensajeFinal = "*" + asunto.trim() + "*\n\n" + mensaje;
            }

            body.put("body", mensajeFinal);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(API_URL_TEXT, request, String.class);

            logger.info("¡Mensaje entregado! Respuesta de Whapi (Código {}): {}", response.getStatusCode().value(), response.getBody());

        } catch (RestClientResponseException e) {
            logger.error("❌ Error de API Whapi al mandar texto (Código {}): {}", e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("❌ Explotó el envío del mensaje a {}: {}", numero, e.getMessage(), e);
        }
    }
}