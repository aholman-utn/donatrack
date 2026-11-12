package com.tp.donatrack.logistica.clients;

import com.tp.commons.config.ServiceToken;
import com.tp.commons.dtos.logistica.SegmentoLogistico;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;

@Component
public class DonacionesPanelClient {
    private static final ParameterizedTypeReference<List<SegmentoLogistico>> SEGMENTOS = new ParameterizedTypeReference<>() {};
    private final RestClient http;

    public DonacionesPanelClient(@Value("${services.donaciones.url:http://localhost:8080}") String url, ServiceToken token) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(8000);
        http = RestClient.builder().baseUrl(url + "/interno/logistica").requestFactory(factory)
                .requestInterceptor((request, body, next) -> {
                    request.getHeaders().setBearerAuth(token.emitir("LOGISTICA_DONACIONES"));
                    return next.execute(request, body);
                }).build();
    }

    public List<SegmentoLogistico> disponibles() {
        return ejecutar(() -> http.get().uri("/disponibles").retrieve().body(SEGMENTOS));
    }

    public List<SegmentoLogistico> estados(List<String> rutas) {
        return ejecutar(() -> http.post().uri("/estados").body(rutas).retrieve().body(SEGMENTOS));
    }

    public List<SegmentoLogistico> reservar(String ruta, List<Long> segmentos) {
        return ejecutar(() -> http.post().uri("/{ruta}/reservar", ruta).body(segmentos).retrieve().body(SEGMENTOS));
    }

    public void iniciar(String ruta, List<Long> segmentos) {
        ejecutar(() -> http.post().uri("/{ruta}/iniciar", ruta).body(segmentos).retrieve().toBodilessEntity());
    }

    public void llegada(String ruta, Long segmento) {
        ejecutar(() -> http.post().uri("/{ruta}/{id}/llegada", ruta, segmento).retrieve().toBodilessEntity());
    }

    private <T> T ejecutar(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (RestClientResponseException error) {
            if (error.getStatusCode().value() == 409) {
                var detalle = error.getResponseBodyAs(ProblemDetail.class);
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        detalle == null ? "El estado de la donación cambió." : detalle.getDetail());
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Donaciones no pudo completar la operación.");
        } catch (ResourceAccessException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Donaciones no está disponible. Volvé a intentar.");
        }
    }
}
