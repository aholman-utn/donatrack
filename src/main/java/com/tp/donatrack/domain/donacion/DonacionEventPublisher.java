package com.tp.donatrack.domain.donacion;

/**
 * Interfaz del publicador de eventos del dominio de donaciones.
 * Permite al dominio disparar eventos sin acoplarse a tecnologías específicas (Spring, colas de mensajes, etc.).
 */
public interface DonacionEventPublisher {
    void publicar(DonacionEntregadaEvent event);
}
