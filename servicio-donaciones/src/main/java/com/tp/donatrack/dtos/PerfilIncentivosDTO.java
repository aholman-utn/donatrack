package com.tp.donatrack.dtos;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilIncentivosDTO {
    private Integer donanteId;
    private int totalDonacionesExitosas;
    private int entidadesAyudadasCount;
    private List<Integer> entidadesAyudadasIds;
    private String categoriaDonante;
    private int posicionRanking;
    private List<RegistroDonacionMensualDTO> comparacionesMensuales;
    private List<InsigniaDTO> insigniasGanadas;
    private MisionDTO misionActual;
    private List<MisionDTO> todasLasMisiones;
    private Boolean visibilidadInsignia;
}
