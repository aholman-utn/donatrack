package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.notificador.TipoNotificador;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionRequestDTO {
    
    @NotBlank
    private String contacto;

    private String titulo;
    
    @NotBlank
    private String cuerpo;
    
    private String asunto;

    @NotNull
    private TipoNotificador tipo;
}