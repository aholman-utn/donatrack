package com.tp.donatrack.logistica.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "eventos_logistica")
public class EventoLogistica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "donacion_segmentada_id")
    private Long donacionSegmentadaId;

    @Column(name = "entidad_beneficiaria_id")
    private Long entidadBeneficiariaId;

    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String detalles;
}
