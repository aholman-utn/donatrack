package com.tp.donatrack.logistica.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "envios")
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parada_id")
    @JsonIgnore
    private Parada parada;

    @Column(name = "donacion_segmentada_id")
    private Long donacionSegmentadaId;

    @Column(name = "entidad_beneficiaria_id")
    private Long entidadBeneficiariaId;

    @Column(name = "ruta_id")
    private Long rutaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
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