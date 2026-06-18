package com.tp.incentivos.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrearPerfilDTO {

    @NotNull(message = "El ID del donante no puede ser nulo")
    private Integer donanteId;

    private String nombreUsuario;
}
