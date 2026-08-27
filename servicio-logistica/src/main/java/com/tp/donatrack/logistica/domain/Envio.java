package com.tp.donatrack.logistica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Envio {
    private Long id;
    private Long donacionSegmentadaId;
    private Long entidadBeneficiariaId;
    private Long rutaId;
    private EstadoEnvio estado;

    public void registrarEnDestino() {
        if (this.estado != EstadoEnvio.EN_TRASLADO && this.estado != EstadoEnvio.ASIGNACION_REALIZADA) {
            throw new IllegalStateException("El envío debe estar en traslado (o asignado) para marcar llegada a destino. Estado actual: " + this.estado);
        }
        this.estado = EstadoEnvio.EN_DESTINO;
    }

    public void registrarRecepcionExitosa() {
        if (this.estado != EstadoEnvio.EN_DESTINO) {
            throw new IllegalStateException("El envío debe estar en destino para confirmar recepción. Estado actual: " + this.estado);
        }
        this.estado = EstadoEnvio.ENTREGADA;
    }

    public void registrarRecepcionFallida() {
        if (this.estado != EstadoEnvio.EN_DESTINO && this.estado != EstadoEnvio.EN_TRASLADO) {
            throw new IllegalStateException("Solo se puede fallar un envío que está en traslado o en destino. Estado actual: " + this.estado);
        }
        this.estado = EstadoEnvio.NO_RECIBIDA;
    }
}