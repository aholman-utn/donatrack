package com.tp.commons.dtos.logistica;

import java.time.LocalDateTime;

public record SegmentoLogistico(Long id, Long entidadId, String entidad, String direccion,
        int cantidad, String unidad, String estado, String rutaId, LocalDateTime llegada) {}
