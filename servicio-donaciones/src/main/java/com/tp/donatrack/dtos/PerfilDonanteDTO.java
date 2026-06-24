package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.donante.CategoriaDonante;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilDonanteDTO {
    private boolean visibilidadInsignia;
    private List<ItemDonacionDTO> historialDonaciones;
    private CategoriaDonante categoriaDonante;
}
