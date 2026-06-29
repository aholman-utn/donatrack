package com.tp.donatrack.services;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.commons.dtos.incentivos.EvaluacionMisionResponseDTO;
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
        requestBody.put("ultimaMisionId", event.dto().getUltimaMisionId());
        requestBody.put("progreso", event.dto().getProgreso());

        try {
            // Log para ver qué estamos enviando
            logger.debug("Enviando request a Incentivos: {}", requestBody);

            var responseEntity = restTemplate.postForEntity(
                    incentivosUrl,
                    requestBody,
                    EvaluacionMisionResponseDTO.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                EvaluacionMisionResponseDTO response = responseEntity.getBody();
                logger.info("Respuesta exitosa de Incentivos. Progreso: {}, Misión Cumplida: {}",
                        response.getNuevoProgreso(), response.isMisionCumplida());

                Donante donante = donanteRepository.findById(event.dto().getDonanteId());

                if (donante == null) {
                    logger.error("Error: Donante con ID {} no encontrado en memoria.", event.dto().getDonanteId());
                    return;
                }

                donante.getPerfil().setProgreso(response.getNuevoProgreso());

                donante.getPerfil().setMisionActualId(response.getSiguienteMisionId());

                if (response.getInsigniaGanada() != null) {
                    logger.info("Donante ganó insignia: {}", response.getInsigniaGanada());
                    donante.getPerfil().getInsignasGanadas().add(response.getInsigniaGanada());
                }

                if (response.isSubioDeCategoria() && response.getNuevoNivel() != null) {
                    logger.info("Donante subió de nivel: {}", response.getNuevoNivel());
                    donante.getPerfil().setNivelDonante(response.getNuevoNivel());
                }

                this.donanteRepository.update(donante);
                logger.info("Donante ID {} actualizado correctamente en memoria.", donante.getPersona().getId());

            } else {
                logger.warn("Respuesta de Incentivos no exitosa. Status: {}. Body es null? {}",
                        responseEntity.getStatusCode(),
                        (responseEntity.getBody() == null));
            }

        } catch (Exception e) {
            logger.error("Error al conectarse con Incentivos: {}", e.getMessage(), e);
        }
    }
}
