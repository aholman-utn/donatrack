package com.tp.donatrack.logistica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoLogistica {
    private String tipoEvento; // "INICIO_RUTA", "ENTREGA_EXITOSA", "ENTREGA_FALLIDA"
    private Long donacionSegmentadaId;
    private Long entidadBeneficiariaId;
    private LocalDateTime timestamp;
    private String detalles;
}
