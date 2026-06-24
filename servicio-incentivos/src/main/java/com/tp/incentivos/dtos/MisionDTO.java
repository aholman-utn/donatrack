package com.tp.incentivos.dtos;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MisionDTO {
    private String titulo;
    private String descripcion;
    private String tipo;
    private int progresoActual;
    private int objetivo;
    private boolean completada;
    private LocalDate fechaObtencion;
}
