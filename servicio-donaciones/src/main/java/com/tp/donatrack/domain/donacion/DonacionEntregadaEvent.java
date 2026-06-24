package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.bien.Categoria;
import java.time.LocalDate;

/**
 * Evento de Dominio que representa que una donación segmentada ha sido
 * entregada.
 * Se utiliza para notificar de manera desacoplada a otros sub-sistemas (ej.
 * Incentivos, Notificaciones).
 */
public record DonacionEntregadaEvent(
                Long donanteId,
                Long entidadBeneficiariaId,
                Categoria categoriaDonacion,
                LocalDate fechaDonacion) {
}
