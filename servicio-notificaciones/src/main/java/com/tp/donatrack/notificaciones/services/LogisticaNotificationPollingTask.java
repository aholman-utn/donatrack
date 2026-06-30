package com.tp.donatrack.notificaciones.services;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
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
public class LogisticaNotificationPollingTask {

    private static final Logger logger = LoggerFactory.getLogger(LogisticaNotificationPollingTask.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final NotificacionService notificacionService;

    // Control de eventos notificados en memoria
    private final Set<String> notifiedEventKeys = ConcurrentHashMap.newKeySet();

    public LogisticaNotificationPollingTask(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Scheduled(fixedDelay = 10000)
    public void sondearYNotificar() {
        String url = "http://localhost:8083/api/logistica/rutas/eventos";
        try {
            logger.info("Notificador sondeando eventos de logística en {}", url);
            EventoLogisticaDTO[] eventos = restTemplate.getForObject(url, EventoLogisticaDTO[].class);
            if (eventos != null && eventos.length > 0) {
                Arrays.stream(eventos).forEach(this::procesarYNotificar);
            }
        } catch (Exception e) {
            logger.warn("Notificador no pudo conectar con el servicio de logística: {}. Reintentando...", e.getMessage());
        }
    }

    private void procesarYNotificar(EventoLogisticaDTO evento) {
        String key = evento.getDonacionSegmentadaId() + "_" + evento.getTipoEvento() + "_" + evento.getTimestamp();
        if (notifiedEventKeys.contains(key)) {
            return;
        }

        logger.info("Notificador procesando evento: {} para donación ID: {}", 
                evento.getTipoEvento(), evento.getDonacionSegmentadaId());

        try {
            switch (evento.getTipoEvento()) {
                case "INICIO_RUTA" -> notificarInicioRuta(evento);
                case "ENTREGA_EXITOSA" -> notificarEntregaExitosa(evento);
                case "ENTREGA_FALLIDA" -> notificarEntregaFallida(evento);
                default -> logger.warn("Tipo de evento no manejado para notificaciones: {}", evento.getTipoEvento());
            }
            notifiedEventKeys.add(key);
        } catch (Exception e) {
            logger.error("Error al enviar notificaciones para el evento {}: {}", evento.getTipoEvento(), e.getMessage(), e);
        }
    }

    private void notificarInicioRuta(EventoLogisticaDTO evento) {
        String linkMapa = "http://localhost:8083/mapa/" + evento.getDonacionSegmentadaId();

        // 1. Notificar a Donante
        try {
            NotificacionRequestDTO contactoDonante = obtenerContactoDonante(evento.getDonacionSegmentadaId());
            if (contactoDonante != null) {
                contactoDonante.setAsunto("Donación en Traslado");
                contactoDonante.setMensaje("Tu donación segmentada ID: " + evento.getDonacionSegmentadaId() 
                        + " ya está en camino a su destino. Podés seguir el camión aquí: " + linkMapa);
                notificacionService.notificar(contactoDonante);
            }
        } catch (Exception e) {
            logger.error("No se pudo notificar al donante para INICIO_RUTA: {}", e.getMessage());
        }

        // 2. Notificar a Entidad Beneficiaria
        try {
            NotificacionRequestDTO contactoEntidad = obtenerContactoEntidad(evento.getEntidadBeneficiariaId());
            if (contactoEntidad != null) {
                contactoEntidad.setAsunto("Envío en Camino");
                contactoEntidad.setMensaje("Una donación segmentada ID: " + evento.getDonacionSegmentadaId() 
                        + " asignada a tu entidad ha iniciado su traslado. Seguir en mapa: " + linkMapa);
                notificacionService.notificar(contactoEntidad);
            }
        } catch (Exception e) {
            logger.error("No se pudo notificar a la entidad para INICIO_RUTA: {}", e.getMessage());
        }
    }

    private void notificarEntregaExitosa(EventoLogisticaDTO evento) {
        // Notificar a Entidad (con comprobante de entrega)
        try {
            NotificacionRequestDTO contactoEntidad = obtenerContactoEntidad(evento.getEntidadBeneficiariaId());
            if (contactoEntidad != null) {
                contactoEntidad.setAsunto("Comprobante de Entrega Recibido");
                contactoEntidad.setMensaje("¡Entrega Confirmada! Se ha recibido la donación segmentada ID: " 
                        + evento.getDonacionSegmentadaId() + ".\nComprobante de entrega:\n"
                        + "Fecha/Hora: " + evento.getTimestamp() + "\n"
                        + "Detalles: " + evento.getDetalles());
                notificacionService.notificar(contactoEntidad);
            }
        } catch (Exception e) {
            logger.error("No se pudo notificar a la entidad para ENTREGA_EXITOSA: {}", e.getMessage());
        }
    }

    private void notificarEntregaFallida(EventoLogisticaDTO evento) {
        // 1. Notificar a Donante
        try {
            NotificacionRequestDTO contactoDonante = obtenerContactoDonante(evento.getDonacionSegmentadaId());
            if (contactoDonante != null) {
                contactoDonante.setAsunto("Alerta de Entrega: Fallida");
                contactoDonante.setMensaje("La entrega de tu donación segmentada ID: " + evento.getDonacionSegmentadaId() 
                        + " ha fallado. Detalle: " + evento.getDetalles() + ". El envío retornará al depósito.");
                notificacionService.notificar(contactoDonante);
            }
        } catch (Exception e) {
            logger.error("No se pudo notificar al donante para ENTREGA_FALLIDA: {}", e.getMessage());
        }

        // 2. Notificar a Entidad Beneficiaria
        try {
            NotificacionRequestDTO contactoEntidad = obtenerContactoEntidad(evento.getEntidadBeneficiariaId());
            if (contactoEntidad != null) {
                contactoEntidad.setAsunto("Alerta de Entrega: Fallida");
                contactoEntidad.setMensaje("La entrega programada para tu entidad de la donación segmentada ID: " 
                        + evento.getDonacionSegmentadaId() + " ha fallado. Detalle: " + evento.getDetalles());
                notificacionService.notificar(contactoEntidad);
            }
        } catch (Exception e) {
            logger.error("No se pudo notificar a la entidad para ENTREGA_FALLIDA: {}", e.getMessage());
        }

        // 3. Notificar a Administradores
        try {
            NotificacionRequestDTO adminNotif = new NotificacionRequestDTO();
            adminNotif.setMedio(TipoNotificador.EMAIL);
            adminNotif.setDestinatario("admin@donatrack.com");
            adminNotif.setAsunto("ALERTA CRÍTICA: Entrega Fallida");
            adminNotif.setMensaje("Alerta del sistema: Se reportó una entrega fallida en logística.\n"
                    + "Donación Segmentada ID: " + evento.getDonacionSegmentadaId() + "\n"
                    + "Entidad Beneficiaria ID: " + evento.getEntidadBeneficiariaId() + "\n"
                    + "Motivo reportado: " + evento.getDetalles() + "\n"
                    + "Fecha/Hora: " + evento.getTimestamp());
            adminNotif.setIdPersona(9999L); // ID ficticio para administradores
            notificacionService.notificar(adminNotif);
        } catch (Exception e) {
            logger.error("No se pudo notificar a los administradores para ENTREGA_FALLIDA: {}", e.getMessage());
        }
    }

    private NotificacionRequestDTO obtenerContactoDonante(Long donacionSegmentadaId) {
        String url = "http://localhost:8080/donaciones-segmentadas/" + donacionSegmentadaId + "/contacto-donante";
        try {
            return restTemplate.getForObject(url, NotificacionRequestDTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener contacto del donante para donación segmentada {}: {}", donacionSegmentadaId, e.getMessage());
            return null;
        }
    }

    private NotificacionRequestDTO obtenerContactoEntidad(Long entidadId) {
        String url = "http://localhost:8080/entidadBeneficiaria/" + entidadId + "/contacto-notificacion";
        try {
            return restTemplate.getForObject(url, NotificacionRequestDTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener contacto de la entidad {}: {}", entidadId, e.getMessage());
            return null;
        }
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
