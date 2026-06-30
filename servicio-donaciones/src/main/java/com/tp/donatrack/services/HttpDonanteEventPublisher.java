package com.tp.donatrack.services;

import com.tp.donatrack.domain.donante.DonanteCreadoEvent;
import com.tp.donatrack.domain.donante.DonanteEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpDonanteEventPublisher implements DonanteEventPublisher {

    private final RestTemplate restTemplate;
    private final String incentivosUrl;

    public HttpDonanteEventPublisher(
            RestTemplate restTemplate,
            @Value("${services.incentivos.url}") String incentivosUrl) {
        this.restTemplate = restTemplate;
        this.incentivosUrl = incentivosUrl.replace("/entrega", ""); // Asume que termina en /entrega por como esta en property, pero si apunta a /perfil
    }

    @Override
    public void publicar(DonanteCreadoEvent event) {
        /*Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("donanteId", event.donanteId());
        requestBody.put("nombreUsuario", event.nombreUsuario());

        try {
            // El endpoint de creacion sera POST /perfil
            // Aseguramos la URL correcta ya que incentivosUrl en properties podria ser especifica
            String url = incentivosUrl;
            if (url.endsWith("/entrega")) {
                url = url.substring(0, url.lastIndexOf("/entrega"));
            }
            if (!url.endsWith("/perfil")) {
                url += "/perfil";
            }
            
            restTemplate.postForEntity(url, requestBody, Void.class);
        } catch (Exception e) {
            // Capturamos la excepción para ser resilientes si incentivos cae
            System.err.println("Error al notificar al servicio de incentivos sobre nuevo donante: " + e.getMessage());
        }*/
    }
}
