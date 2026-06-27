package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.bien.CategoriaBien;
import com.tp.donatrack.dtos.DonacionEntregadaEventDTO;

import java.time.LocalDate;

/**
 * Evento de Dominio que representa que una donación segmentada ha sido
 * entregada.
 * Se utiliza para notificar de manera desacoplada a otros sub-sistemas (ej.
 * Incentivos, Notificaciones).
 */
public record DonacionEntregadaEvent(
        DonacionEntregadaEventDTO dto
    ) {
}
