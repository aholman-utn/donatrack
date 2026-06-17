package com.tp.incentivos.dtos;

import com.tp.incentivos.domain.CategoriaBien;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntregaDonacionDTO {

    @NotNull(message = "El ID del donante no puede ser nulo")
    private Integer donanteId;

    @NotNull(message = "El ID de la entidad beneficiaria no puede ser nulo")
    private Integer entidadBeneficiariaId;

    private int cantidadBienes;
    private LocalDate fechaDonacion;
    private CategoriaBien categoriaDonacion;
    private String nombreUsuario;
}
