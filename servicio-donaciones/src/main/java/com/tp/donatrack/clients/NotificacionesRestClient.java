package com.tp.donatrack.clients;

import com.tp.commons.config.ServiceToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Consulta al servicio de notificaciones el historial de avisos de una persona
 * (donante o persona jurídica de una entidad) usando un token de servicio.
 */
@Component
public class NotificacionesRestClient {

    private final RestTemplate restTemplate;
    private final ServiceToken serviceToken;

    @Value("${services.notificaciones.url:http://localhost:8082}")
    private String notificacionesUrl;

    public NotificacionesRestClient(RestTemplate restTemplate, ServiceToken serviceToken) {
        this.restTemplate = restTemplate;
        this.serviceToken = serviceToken;
    }

    public List<Aviso> avisosDe(Long idPersona) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken.emitir("DONACIONES_NOTIFICACIONES"));
        List<Aviso> avisos = restTemplate.exchange(notificacionesUrl + "/api/notificaciones/personas/{id}",
                HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<Aviso>>() {}, idPersona)
                .getBody();
        if (avisos == null) return List.of();
        return avisos.stream()
                .sorted(Comparator.comparing(Aviso::fecha, Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .toList();
    }

    public record Aviso(Long id, String asunto, String mensaje, LocalDateTime fecha) {}
}
