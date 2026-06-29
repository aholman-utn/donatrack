package com.tp.donatrack.dtos;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionEntregadaEventDTO {
    private Long donacionSegmentadaId;
    private Long donanteId;
    private Long ultimaMisionId;
    private double progreso;
    private com.tp.commons.domain.donantes.Nivel categoriaDonante;
    private String nombreDonante;
}
