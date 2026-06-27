package com.tp.incentivos.dtos;

import jakarta.validation.constraints.NotNull;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntregaDonacionDTO {
    @NotNull(message = "El ID del donante no puede ser nulo")
    private Long donanteId;

    @NotNull(message = "El ID de la donación segentada no puede ser nulo")
    private Long donacionSegmentadaId;

    private Long ultimaMisionId;

    @NotNull(message = "El progreso no puede ser nulo")
    private double progreso;
}
