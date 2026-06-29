package com.tp.incentivos.clients;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class DonacionesRestClient {

    // 1. Lo instanciás vos a mano igual que en el otro servicio
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.donaciones.url}")
    private String donacionesUrl;

    public DonacionesRestClient() {}

    public IndicadoresDonanteDTO obtenerIndicadores(
            Long donanteId,
            Long donacionSegmentadaId,
            List<String> indicadores
    ) {
        String url = UriComponentsBuilder.fromHttpUrl(donacionesUrl)
                .path("/donaciones-segmentadas/indicadores/{donacionSegmentadaId}")
                .queryParam("donanteId", donanteId)
                .queryParam("indicadores", String.join(",", indicadores))
                .buildAndExpand(donacionSegmentadaId)
                .toUriString();

        return restTemplate.getForObject(url, IndicadoresDonanteDTO.class);
    }
}