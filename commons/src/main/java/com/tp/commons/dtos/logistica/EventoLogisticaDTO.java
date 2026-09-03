package com.tp.commons.dtos.logistica;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoLogisticaDTO {
    private TipoEventoLogistica tipoEvento;
    private Long donacionSegmentadaId;
    private Long entidadBeneficiariaId;
    private LocalDateTime timestamp;
    private String detalles;
}
