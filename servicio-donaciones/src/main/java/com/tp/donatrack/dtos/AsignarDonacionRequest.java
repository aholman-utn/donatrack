package com.tp.donatrack.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request para asignar una donación segmentada a una entidad beneficiaria
 * seleccionada del ranking de matchmaking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignarDonacionRequest {

    @NotNull(message = "El ID de la donación segmentada es obligatorio")
    private Integer donacionSegmentadaId;

    @NotNull(message = "El ID de la entidad beneficiaria es obligatorio")
    private Integer entidadBeneficiariaId;
}
