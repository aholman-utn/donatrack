package com.tp.donatrack.domain.donacion;

/**
 * Evento de Dominio que representa que una donación segmentada ha sido entregada.
 * Se utiliza para notificar de manera desacoplada a otros sub-sistemas (ej. Incentivos, Notificaciones).
 */
public record DonacionEntregadaEvent(
    Integer donanteId,
    String actorConfirmacion
) {}
