package com.tp.commons.dtos.logistica;
import com.tp.commons.enums.Unidad;

public record DonacionSegmentadaListaParaEntregarALogisticaDTO(
        Long donacionSegmentadaId,
        Long entidadBeneficiariaId,
        String direccionEntidadBeneficiaria,
        Integer cantidad,
        Unidad unidad
) {}