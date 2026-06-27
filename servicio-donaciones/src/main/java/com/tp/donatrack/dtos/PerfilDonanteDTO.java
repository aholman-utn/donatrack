package com.tp.donatrack.dtos;

import java.util.List;

import com.tp.commons.domain.donantes.Nivel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilDonanteDTO {
    private boolean visibilidadInsignia;
    private List<ItemDonacionDTO> historialDonaciones;
    private Nivel categoriaDonante;
}
