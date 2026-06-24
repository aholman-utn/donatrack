package com.tp.incentivos.domain;

import lombok.*;
//   Se usa para comparaciones mensuales y para la misión Racha.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistroDonacionMensual {
    private int anio;
    private int mes; // 1-12
    private int totalDonaciones;

    public RegistroDonacionMensual(int anio, int mes) {
        this.anio = anio;
        this.mes = mes;
        this.totalDonaciones = 0;
    }

    public void registrar() {
        this.totalDonaciones++;
    }
}
