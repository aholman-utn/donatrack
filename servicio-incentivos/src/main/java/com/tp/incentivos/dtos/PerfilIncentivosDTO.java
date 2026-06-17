package com.tp.incentivos.dtos;

import java.util.List;
import java.util.Set;
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
    private Set<Integer> entidadesAyudadasIds;
    private String categoriaDonante;
    private int posicionRanking;
    private List<RegistroDonacionMensualDTO> comparacionesMensuales;
    private List<InsigniaDTO> insigniasGanadas;
    private MisionDTO misionActual;
    private List<MisionDTO> todasLasMisiones;
}
