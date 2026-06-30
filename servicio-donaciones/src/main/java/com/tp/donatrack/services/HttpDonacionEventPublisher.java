package com.tp.donatrack.services;

import com.tp.commons.dtos.incentivos.EvaluacionMisionResponseDTO;
import com.tp.donatrack.domain.donacion.DonacionEntregadaEvent;
import com.tp.donatrack.domain.donacion.DonacionEventPublisher;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.donante.Metrica;
import com.tp.donatrack.repositories.DonanteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(HttpDonacionEventPublisher.class);

    public HttpDonacionEventPublisher(
            RestTemplate restTemplate,
            @Value("${services.incentivos.url}") String incentivosUrl,
            DonanteRepository donanteRepository
    ) {
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
        requestBody.put("categoriaDonante", event.dto().getCategoriaDonante());
        requestBody.put("nombreDonante", event.dto().getNombreDonante());

        try {
            // Log para ver qué estamos enviando
            logger.debug("Enviando request a Incentivos: {}", requestBody);

            String url = incentivosUrl + "/entrega";

            var responseEntity = restTemplate.postForEntity(
                    url,
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

                if (response.getInsigniaGanada() != null) {
                    logger.info("Donante ganó insignia: {}", response.getInsigniaGanada().getTitulo());
                    donante.getPerfil().getInsigniasGanadas().add(response.getInsigniaGanada().getTitulo());
                }

                // Registrar misión completada en métricas
                if (response.isMisionCumplida()) {
                    Metrica metricaDonante = donante.getPerfil().getMetricasPerfil();
                    logger.info("Donante ganó mision: {}", event.dto().getUltimaMisionId());
                    metricaDonante.getMisionesCompletadas().add(donante.getPerfil().getMisionActualId());
                }

                if (response.isSubioDeCategoria() && response.getNuevoNivel() != null) {
                    logger.info("Donante subió de nivel: {}", response.getNuevoNivel());
                    donante.getPerfil().setNivelDonante(response.getNuevoNivel());
                }

                donante.getPerfil().setProgreso(response.getNuevoProgreso());

                donante.getPerfil().setMisionActualId(response.getSiguienteMisionId());

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
