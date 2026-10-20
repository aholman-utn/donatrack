package com.tp.donatrack.logistica.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "eventos_logística")
public class EventoLogistica {
    @Id
    private String id = null;

    private String tipoEvento;

    private Long donacionSegmentadaId;

    private Long entidadBeneficiariaId;

    private LocalDateTime timestamp;

    private String detalles;
}
