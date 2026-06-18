package com.tp.donatrack.dtos.necesidad;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrearNecesidadDTO {

    @NotNull(message = "El ID de la Entidad Beneficiaria es requerido")
    private Long entidadBeneficiariaId;

    @NotNull(message = "El nombre de la subcategoría es requerido")
    private String subCategoriaNombre;

    @NotNull(message = "La cantidad solicitada es requerida")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "La fecha límite es requerida")
    @FutureOrPresent(message = "La fecha límite debe ser actual o futura")
    private LocalDate fechaLimite;
    
    @NotNull(message = "El tipo de necesidad es requerido (RECURRENTE o EXTRAORDINARIA)")
    private String tipoNecesidad;
    
    private Integer diasRecurrencia; // Solo para recurrentes
    private String causa; // Solo para extraordinarias
}
