package com.tp.donatrack.services;

import com.tp.commons.services.notificador.NotificacionRestClient;
import com.tp.donatrack.domain.donacion.DonacionEntregadaEvent;
import com.tp.donatrack.domain.donacion.DonacionEventPublisher;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.repositories.DonanteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpDonacionEventPublisher implements DonacionEventPublisher {

    private final RestTemplate restTemplate;
    private final String incentivosUrl;
    private final DonanteRepository donanteRepository;
    private static final Logger logger = LoggerFactory.getLogger(HttpDonacionEventPublisher.class);

    public HttpDonacionEventPublisher(
            RestTemplate restTemplate,
            @Value("${services.incentivos.url}") String incentivosUrl,
            DonanteRepository donanteRepository) {
        this.restTemplate = restTemplate;
        this.incentivosUrl = incentivosUrl;
        this.donanteRepository = donanteRepository;
    }

    @Override
    public void publicar(DonacionEntregadaEvent event) {
        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("donacionSegmentadaId", event.dto().getDonacionSegmentadaId());
        requestBody.put("donanteId", event.dto().getDonanteId());
        requestBody.put("ultimaMisionId", event.dto().getMisionId());
        requestBody.put("progreso", event.dto().getProgreso());
        try {
            restTemplate.postForEntity(incentivosUrl, requestBody, Void.class);
        } catch (Exception e) {
            logger.error("Error al conectase con servicio de incentivos: {}", e.getMessage());
        }
    }
}
