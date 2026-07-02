package com.tp.donatrack.clients;

import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class LogisticaRestClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.logistica.url}")
    private String logisticaUrl;

    public LogisticaRestClient() {}

    public void enviarLoteDonaciones(List<DonacionSegmentadaListaParaEntregarALogisticaDTO> lote) {
        String url = UriComponentsBuilder.fromHttpUrl(logisticaUrl)
                .path("/planificar")
                .build()
                .toUriString();

        try {
            restTemplate.postForObject(url, lote, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Falló la comunicación con el servicio de logística: " + e.getMessage(), e);
        }
    }
}