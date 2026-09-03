package com.tp.donatrack.services;

import com.tp.commons.config.RabbitMQCommonsConfig;
import com.tp.commons.dtos.logistica.EventoLogisticaDTO;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.repositories.DonacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LogisticaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(LogisticaEventListener.class);
    private final DonacionRepository donacionRepository;
    private final TrazabilidadService trazabilidadService;

    public LogisticaEventListener(DonacionRepository donacionRepository, TrazabilidadService trazabilidadService) {
        this.donacionRepository = donacionRepository;
        this.trazabilidadService = trazabilidadService;
    }

    @RabbitListener(queues = RabbitMQCommonsConfig.COLA_EVENTOS_DONACIONES, concurrency = "1")
    public void recibirEventoLogistica(EventoLogisticaDTO evento) {
        logger.info("Procesando evento logístico desde RabbitMQ: {} para donación segmentada ID: {}",
                evento.getTipoEvento(), evento.getDonacionSegmentadaId());

        try {
            switch (evento.getTipoEvento()) {
                case INICIO_RUTA -> procesarInicioRuta(evento);
                case LLEGADA_A_DESTINO -> procesarLlegadaADestino(evento);
                case ENTREGA_EXITOSA -> procesarEntregaExitosa(evento);
                case ENTREGA_FALLIDA -> procesarEntregaFallida(evento);
                default -> logger.warn("Tipo de evento desconocido: {}", evento.getTipoEvento());
            }
        } catch (Exception e) {
            // El evento se consume igual: sin DLQ configurada, propagar la excepción
            // haría que RabbitMQ lo re-encole indefinidamente.
            logger.error("Error al procesar el evento logístico {} para la donación {}: {}",
                    evento.getTipoEvento(), evento.getDonacionSegmentadaId(), e.getMessage(), e);
        }
    }

    private void procesarInicioRuta(EventoLogisticaDTO evento) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(evento.getDonacionSegmentadaId());
        if (segmentada != null) {
            if (segmentada.getEstado() == EstadoDonacionSegmentada.EN_PLANIFICACION) {
                segmentada.iniciarTraslado("Sistema (RabbitMQ Listener)");
                logger.info("Donación segmentada ID {} transicionada a EN_TRASLADO", segmentada.getId());

                try {
                    trazabilidadService.notificarInicioDeRuta(segmentada);
                } catch (Exception notifEx) {
                    logger.error("El estado avanzó, pero falló la notificación de INICIO_RUTA: {}", notifEx.getMessage());
                }
            } else {
                logger.warn("Transición rechazada. La donación ID {} está en estado {}, no se puede iniciar ruta.", segmentada.getId(), segmentada.getEstado());
            }
        } else {
            logger.warn("Donación segmentada ID {} no encontrada localmente", evento.getDonacionSegmentadaId());
        }
    }

    private void procesarEntregaFallida(EventoLogisticaDTO evento) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(evento.getDonacionSegmentadaId());
        if (segmentada != null) {
            if (segmentada.getEstado() == EstadoDonacionSegmentada.EN_TRASLADO) {
                String motivo = evento.getDetalles() != null ? evento.getDetalles() : "Entrega fallida reportada por logística";
                segmentada.registrarEntregaFallida("Sistema (RabbitMQ)", motivo);
                logger.info("Entrega fallida registrada para donación segmentada ID {}", evento.getDonacionSegmentadaId());
                trazabilidadService.notificarEntregaNoSatisfactoria(segmentada, motivo);
            } else {
                logger.warn("Transición rechazada. La donación ID {} está en estado {}, no se puede registrar entrega fallida.", segmentada.getId(), segmentada.getEstado());
            }
        } else {
            logger.warn("Donación segmentada ID {} no encontrada localmente", evento.getDonacionSegmentadaId());
        }
    }

    private void procesarEntregaExitosa(EventoLogisticaDTO evento) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(evento.getDonacionSegmentadaId());

        if (segmentada != null) {
            if (segmentada.getEstado() == EstadoDonacionSegmentada.EN_TRASLADO || segmentada.getEstado() == EstadoDonacionSegmentada.PENDIENTE_RECEPCION) {
                Donacion donacionPadre = donacionRepository.findDonacionByDonacionesSegmentadaId(segmentada.getId());

                if (donacionPadre != null) {
                    trazabilidadService.recepcionarEntrega(
                            donacionPadre.getId(),
                            Math.toIntExact(segmentada.getId()),
                            evento.getTimestamp(),
                            evento.getDetalles()
                    );
                    logger.info("Donación segmentada ID {} procesada como ENTREGADA", segmentada.getId());
                } else {
                    logger.warn("No se encontró la Donación padre para el segmento ID {}", segmentada.getId());
                }
            } else {
                logger.warn("Transición rechazada. La donación ID {} está en estado {}, no se puede registrar entrega exitosa.", segmentada.getId(), segmentada.getEstado());
            }
        } else {
            logger.warn("Donación segmentada ID {} no encontrada localmente", evento.getDonacionSegmentadaId());
        }
    }

    private void procesarLlegadaADestino(EventoLogisticaDTO evento) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(evento.getDonacionSegmentadaId());
        if (segmentada != null) {
            segmentada.registrarLlegadaADestino("Sistema (RabbitMQ Listener)");
            logger.info("Donación segmentada ID {} marcada como en destino (esperando confirmación)", segmentada.getId());
        } else {
            logger.warn("Donación segmentada ID {} no encontrada localmente", evento.getDonacionSegmentadaId());
        }
    }
}
