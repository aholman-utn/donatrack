package com.tp.incentivos.clients;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class DonacionesRestClient {
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

    public List<java.util.Map<String, Object>> obtenerTodosDonantes() {
        String url = UriComponentsBuilder.fromHttpUrl(donacionesUrl)
                .path("/donantes")
                .build()
                .toUriString();
        java.util.Map<String, Object>[] array = restTemplate.getForObject(url, java.util.Map[].class);
        if (array == null) {
            return List.of();
        }
        return java.util.Arrays.asList(array);
    }

    public NotificacionRequestDTO obtenerDatosParaNotificar(Long donanteId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(donacionesUrl)
                .path("/donantes/{id}/contacto-notificacion")
                .buildAndExpand(donanteId)
                .toUriString();

            return restTemplate.getForObject(url, NotificacionRequestDTO.class);

        } catch (Exception e) {
            return null;
        }
    }

    public List<com.tp.commons.dtos.incentivos.DonanteRachaDTO> obtenerDonantesConMisionActual() {
        String url = UriComponentsBuilder.fromHttpUrl(donacionesUrl)
                .path("/donantes/ids-mision-actual")
                .build()
                .toUriString();

        com.tp.commons.dtos.incentivos.DonanteRachaDTO[] array = restTemplate.getForObject(
                url, com.tp.commons.dtos.incentivos.DonanteRachaDTO[].class);

        if (array == null) {
            return List.of();
        }
        return java.util.Arrays.asList(array);
    }

    public java.time.LocalDate obtenerFechaUltimaDonacion(Long donanteId) {
        String url = UriComponentsBuilder.fromHttpUrl(donacionesUrl)
                .path("/donantes/{id}/fecha-ultima-donacion")
                .buildAndExpand(donanteId)
                .toUriString();

        try {
            java.util.Map<String, String> response = restTemplate.getForObject(url, java.util.Map.class);
            if (response != null && response.get("fechaUltimaDonacion") != null) {
                return java.time.LocalDate.parse(response.get("fechaUltimaDonacion"));
            }
        } catch (Exception e) {
            // Si falla, retornamos null
        }
        return null;
    }

    public void resetearProgresoRacha(Long donanteId) {
        String url = UriComponentsBuilder.fromHttpUrl(donacionesUrl)
                .path("/donantes/{id}/resetear-progreso-racha")
                .buildAndExpand(donanteId)
                .toUriString();

        restTemplate.put(url, null);
    }
}