package com.tp.incentivos.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDonacionMensualDTO {
    private int anio;
    private int mes;
    private int totalDonaciones;
}
