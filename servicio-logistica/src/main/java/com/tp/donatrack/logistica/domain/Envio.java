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
    private EstadoEnvio estado;

    public void registrarRecepcionExitosa() {
        if (this.estado != EstadoEnvio.EN_TRASLADO && this.estado != EstadoEnvio.ASIGNACION_REALIZADA) {
            throw new IllegalStateException("El envío debe estar en traslado o asignado para poder registrar su recepción");
        }
        this.estado = EstadoEnvio.ENTREGADA;
    }

    public void registrarRecepcionFallida() {
        if (this.estado != EstadoEnvio.EN_TRASLADO && this.estado != EstadoEnvio.ASIGNACION_REALIZADA) {
            throw new IllegalStateException("El envío debe estar en traslado o asignado para poder registrar una falla en su recepción");
        }
        this.estado = EstadoEnvio.NO_RECIBIDA;
    }
}
