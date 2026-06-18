package com.tp.donatrack.dtos.necesidad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NecesidadResponseDTO {
    private Long id;
    private Long entidadBeneficiariaId;
    private String subCategoriaNombre;
    private Integer cantidadSolicitada;
    private Integer cantidadCubierta;
    private Date fechaDelPedido;
    private String estado;
    private boolean activa;
    
    // Campos específicos para subtipos
    private String tipoNecesidad;
    private Integer diasRecurrencia;
    private String causa;
}
