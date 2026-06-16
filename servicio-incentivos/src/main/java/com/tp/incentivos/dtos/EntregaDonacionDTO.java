package com.tp.incentivos.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaDonacionDTO {
    @NotNull(message = "El ID del donante no puede ser nulo")
    private Long donanteId;

    @NotNull(message = "El ID de la entidad beneficiaria no puede ser nulo")
    private Long entidadBeneficiariaId;
}
