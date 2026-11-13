package com.tp.donatrack.services;

import com.tp.commons.dtos.incentivos.EvaluacionMisionResponseDTO;
import com.tp.commons.config.ServiceToken;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import com.tp.donatrack.domain.donacion.DonacionEntregadaEvent;
import com.tp.donatrack.domain.donacion.DonacionEventPublisher;

import com.tp.donatrack.domain.donante.Donante;

import com.tp.donatrack.repositories.DonanteRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpDonacionEventPublisher implements DonacionEventPublisher {

    private final RestTemplate restTemplate;
    private final String incentivosUrl;
    private final DonanteRepository donanteRepository;
    private final ServiceToken serviceToken;

    public HttpDonacionEventPublisher(
            RestTemplate restTemplate,
            @Value("${services.incentivos.url}") String incentivosUrl,
            DonanteRepository donanteRepository, ServiceToken serviceToken) {
        this.restTemplate = restTemplate;
        this.incentivosUrl = incentivosUrl;
        this.donanteRepository = donanteRepository;
        this.serviceToken = serviceToken;
    }

    @Override
    public void publicar(DonacionEntregadaEvent event) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("donacionSegmentadaId", event.dto().getDonacionSegmentadaId());
        requestBody.put("donanteId", event.dto().getDonanteId());
        requestBody.put("ultimaMisionId", event.dto().getUltimaMisionId());
        requestBody.put("progreso", event.dto().getProgreso());
        requestBody.put("categoriaDonante", event.dto().getCategoriaDonante());
        requestBody.put("nombreDonante", event.dto().getNombreDonante());

        aplicarEvaluacion(event, evaluar(requestBody));
    }

    public void evaluarEntrega(Donante donante, Long segmentoId) {
        var perfil = donante.getPerfil();
        if (perfil.getMisionActualId() == null) return;
        var dto = new com.tp.donatrack.dtos.DonacionEntregadaEventDTO();
        dto.setDonacionSegmentadaId(segmentoId);
        dto.setDonanteId(donante.getId());
        dto.setProgreso(perfil.getProgreso() == null ? 0.0 : perfil.getProgreso());
        dto.setUltimaMisionId(perfil.getMisionActualId());
        dto.setCategoriaDonante(perfil.getNivelDonante());
        dto.setNombreDonante(donante.getNombreCompleto());
        publicar(new DonacionEntregadaEvent(dto));
    }

    private EvaluacionMisionResponseDTO evaluar(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken.emitir("DONACIONES_INCENTIVOS"));
        var respuesta = restTemplate.postForEntity(incentivosUrl + "/entrega",
                new HttpEntity<>(requestBody, headers), EvaluacionMisionResponseDTO.class);
        if (respuesta.getBody() == null) throw new IllegalStateException("Incentivos devolvió una respuesta vacía");
        return respuesta.getBody();
    }

    private void aplicarEvaluacion(DonacionEntregadaEvent evento, EvaluacionMisionResponseDTO respuesta) {
        Donante donante = donanteRepository.findByIdOrNull(evento.dto().getDonanteId());
        if (donante == null) throw new IllegalStateException("No se encontró el donante de la entrega");
        var perfil = donante.getPerfil();
        if (respuesta.getInsigniaGanada() != null) {
            perfil.getInsigniasGanadas().add(respuesta.getInsigniaGanada().getTitulo());
        }
        if (respuesta.isMisionCumplida()) {
            perfil.setTotalMisionesCompletadas(perfil.getTotalMisionesCompletadas() + 1);
        }
        if (respuesta.isSubioDeCategoria() && respuesta.getNuevoNivel() != null) {
            perfil.setNivelDonante(respuesta.getNuevoNivel());
        }
        perfil.setProgreso(respuesta.getNuevoProgreso());
        perfil.setMisionActualId(respuesta.getSiguienteMisionId());
        donanteRepository.update(donante);
    }
}
