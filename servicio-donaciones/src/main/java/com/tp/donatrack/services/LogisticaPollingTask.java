package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.repositories.DonacionRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LogisticaPollingTask {

    private static final Logger logger = LoggerFactory.getLogger(LogisticaPollingTask.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final DonacionService donacionService;
    private final DonacionRepository donacionRepository;

    // Control de eventos procesados en memoria
    private final Set<String> eventKeysProcesados = ConcurrentHashMap.newKeySet();

    public LogisticaPollingTask(DonacionService donacionService, DonacionRepository donacionRepository) {
        this.donacionService = donacionService;
        this.donacionRepository = donacionRepository;
    }

    @Scheduled(fixedDelay = 10000)
    public void sondearEventosLogistica() {
        String url = "http://localhost:8083/api/logistica/rutas/eventos";
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

    private void procesarEvento(EventoLogisticaDTO evento) {
        // Generar una clave única para evitar duplicados
        String key = evento.getDonacionSegmentadaId() + "_" + evento.getTipoEvento() + "_" + evento.getTimestamp();
        if (eventKeysProcesados.contains(key)) {
            return;
        }

        logger.info("Procesando evento logístico: {} para donación segmentada ID: {}", 
                evento.getTipoEvento(), evento.getDonacionSegmentadaId());

        try {
            switch (evento.getTipoEvento()) {
                case "INICIO_RUTA" -> procesarInicioRuta(evento);
                case "ENTREGA_EXITOSA" -> procesarEntregaExitosa(evento);
                case "ENTREGA_FALLIDA" -> procesarEntregaFallida(evento);
                default -> logger.warn("Tipo de evento desconocido: {}", evento.getTipoEvento());
            }
            eventKeysProcesados.add(key);
        } catch (Exception e) {
            logger.error("Error al procesar el evento logístico {} para la donación {}: {}", 
                    evento.getTipoEvento(), evento.getDonacionSegmentadaId(), e.getMessage(), e);
        }
    }

    private void procesarInicioRuta(EventoLogisticaDTO evento) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(evento.getDonacionSegmentadaId());
        if (segmentada != null) {
            if (segmentada.getEstado() == EstadoDonacionSegmentada.ASIGNACION_REALIZADA) {
                segmentada.listarParaEntrega("Sistema (Logística Polling)");
            }
            if (segmentada.getEstado() == EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR) {
                segmentada.iniciarTraslado("Sistema (Logística Polling)");
                logger.info("Donación segmentada ID {} transicionada a EN_TRASLADO", segmentada.getId());
            }
        } else {
            logger.warn("Donación segmentada ID {} no encontrada localmente", evento.getDonacionSegmentadaId());
        }
    }

    private void procesarEntregaExitosa(EventoLogisticaDTO evento) {
        donacionService.registrarEntrega(evento.getDonacionSegmentadaId());
        logger.info("Entrega exitosa registrada para donación segmentada ID {}", evento.getDonacionSegmentadaId());
    }

    private void procesarEntregaFallida(EventoLogisticaDTO evento) {
        donacionService.registrarEntregaFallida(evento.getDonacionSegmentadaId(), evento.getDetalles());
        logger.info("Entrega fallida registrada para donación segmentada ID {}", evento.getDonacionSegmentadaId());
    }

    @Data
    public static class EventoLogisticaDTO {
        private String tipoEvento;
        private Long donacionSegmentadaId;
        private Long entidadBeneficiariaId;
        private LocalDateTime timestamp;
        private String detalles;
    }
}
