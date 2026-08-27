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
    //TO DO: tipoEvento tiene que ser un enum.
    private String tipoEvento; // "INICIO_RUTA", "ENTREGA_EXITOSA", "ENTREGA_FALLIDA", "LLEGADA_A_DESTINO"
    private Long donacionSegmentadaId;
    private Long entidadBeneficiariaId;
    private LocalDateTime timestamp;
    private String detalles;
}
