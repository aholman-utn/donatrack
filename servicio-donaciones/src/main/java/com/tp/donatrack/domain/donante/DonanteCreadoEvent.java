package com.tp.donatrack.domain.donante;

/**
 * Evento de Dominio que representa la creación de un nuevo donante.
 * Se utiliza para notificar al servicio de incentivos y otros subsistemas.
 */
public record DonanteCreadoEvent(
        Long donanteId,
        String nombreUsuario) {
}
