package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.repositories.DonacionRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tarea programada que sondea el servicio de logística cada 10 segundos
 * para detectar eventos (inicio de ruta, entrega fallida) y actualizar
 * el estado de las donaciones segmentadas en el servicio de donaciones.
 * Además, dispara las notificaciones correspondientes.
 */
@Component
public class LogisticaPollingTask {

    private static final Logger logger = LoggerFactory.getLogger(LogisticaPollingTask.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final DonacionService donacionService;
    private final DonacionRepository donacionRepository;
    private final TrazabilidadService trazabilidadService;

    @Value("${services.logistica.url}")
    private String logisticaBaseUrl;

    private final Set<String> eventKeysProcesados = ConcurrentHashMap.newKeySet();

    public LogisticaPollingTask(
            DonacionService donacionService,
            DonacionRepository donacionRepository,
            TrazabilidadService trazabilidadService) {
        this.donacionService = donacionService;
        this.donacionRepository = donacionRepository;
        this.trazabilidadService = trazabilidadService;
    }

    /**
     * Consulta periódicamente el endpoint de eventos del servicio de logística.
     * Si detecta eventos nuevos, los procesa y marca como consumidos.
     */
    @Scheduled(fixedDelay = 10000)
    public void sondearEventosLogistica() {
        String url = logisticaBaseUrl + "/api/logistica/rutas/eventos";
        try {
            logger.info("Sondeando eventos de logística en {}", url);
            EventoLogisticaDTO[] eventos = restTemplate.getForObject(url, EventoLogisticaDTO[].class);
            if (eventos != null && eventos.length > 0) {
                Arrays.stream(eventos).forEach(this::procesarEvento);
            }
        } catch (Exception e) {
            logger.warn("No se pudo conectar con el servicio de logística: {}. Reintentando en 10s...", e.getMessage());
        }
    }

    /**
     * Procesa un evento individual de logística.
     * Genera una clave única por evento para evitar procesamiento duplicado.
     */
    private void procesarEvento(EventoLogisticaDTO evento) {
        String key = evento.getDonacionSegmentadaId() + "_" + evento.getTipoEvento() + "_" + evento.getTimestamp();
        if (eventKeysProcesados.contains(key)) {
            return;
        }

        logger.info("Procesando evento logístico: {} para donación segmentada ID: {}",
                evento.getTipoEvento(), evento.getDonacionSegmentadaId());

        try {
            switch (evento.getTipoEvento()) {
                case "INICIO_RUTA" -> procesarInicioRuta(evento);
                case "ENTREGA_FALLIDA" -> procesarEntregaFallida(evento);
                default -> logger.warn("Tipo de evento desconocido: {}", evento.getTipoEvento());
            }
            eventKeysProcesados.add(key);
        } catch (Exception e) {
            logger.error("Error al procesar el evento logístico {} para la donación {}: {}",
                    evento.getTipoEvento(), evento.getDonacionSegmentadaId(), e.getMessage(), e);
        }
    }

    /**
     * Procesa un evento de inicio de ruta: transiciona la donación segmentada
     * a EN_TRASLADO y dispara la notificación al donante y la entidad beneficiaria.
     */
    private void procesarInicioRuta(EventoLogisticaDTO evento) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(evento.getDonacionSegmentadaId());
        if (segmentada != null) {
            if (segmentada.getEstado() == EstadoDonacionSegmentada.ASIGNACION_REALIZADA) {
                segmentada.listarParaEntrega("Sistema (Logística Polling)");
            }
            if (segmentada.getEstado() == EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR) {
                segmentada.iniciarTraslado("Sistema (Logística Polling)");
                logger.info("Donación segmentada ID {} transicionada a EN_TRASLADO", segmentada.getId());
                trazabilidadService.notificarInicioDeRuta(segmentada);
            }
        } else {
            logger.warn("Donación segmentada ID {} no encontrada localmente", evento.getDonacionSegmentadaId());
        }
    }

    /**
     * Procesa un evento de entrega fallida: registra la falla con el motivo,
     * devuelve la donación al depósito y dispara la notificación correspondiente.
     */
    private void procesarEntregaFallida(EventoLogisticaDTO evento) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(evento.getDonacionSegmentadaId());
        if (segmentada != null) {
            String motivo = evento.getDetalles() != null ? evento.getDetalles() : "Entrega fallida reportada por logística";
            segmentada.registrarEntregaFallida("Sistema (Logística)", motivo);
            logger.info("Entrega fallida registrada para donación segmentada ID {}", evento.getDonacionSegmentadaId());
            trazabilidadService.notificarEntregaNoSatisfactoria(segmentada, motivo);
        } else {
            logger.warn("Donación segmentada ID {} no encontrada localmente", evento.getDonacionSegmentadaId());
        }
    }

    /**
     * DTO que representa un evento publicado por el servicio de logística.
     */
    @Data
    public static class EventoLogisticaDTO {
        private String tipoEvento;
        private Long donacionSegmentadaId;
        private Long entidadBeneficiariaId;
        private LocalDateTime timestamp;
        private String detalles;
    }
}
