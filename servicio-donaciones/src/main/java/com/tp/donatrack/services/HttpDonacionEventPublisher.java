package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.DonacionEntregadaEvent;
import com.tp.donatrack.domain.donacion.DonacionEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpDonacionEventPublisher implements DonacionEventPublisher {

    private final RestTemplate restTemplate;
    private final String incentivosUrl;

    public HttpDonacionEventPublisher(
            RestTemplate restTemplate,
            @Value("${services.incentivos.url}") String incentivosUrl) {
        this.restTemplate = restTemplate;
        this.incentivosUrl = incentivosUrl;
    }

    @Override
    public void publicar(DonacionEntregadaEvent event) {
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("donanteId", event.donanteId());
        requestBody.put("entidadBeneficiariaId", event.entidadBeneficiariaId());
        
        try {
            restTemplate.postForEntity(incentivosUrl, requestBody, Void.class);
        } catch (Exception e) {
            // Se captura la excepcion para que no corte el flujo principal si el microservicio esta caido
            System.err.println("Error al notificar al servicio de incentivos: " + e.getMessage());
        }
    }
}
