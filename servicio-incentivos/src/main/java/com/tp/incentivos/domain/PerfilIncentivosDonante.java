package com.tp.incentivos.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilIncentivosDonante {
    private Long donanteId;
    private int totalDonacionesExitosas;
    
    @Builder.Default
    private Set<Long> entidadesAyudadasIds = new HashSet<>();

    public void registrarEntrega(Long entidadBeneficiariaId) {
        this.totalDonacionesExitosas++;
        if (entidadBeneficiariaId != null) {
            this.entidadesAyudadasIds.add(entidadBeneficiariaId);
        }
    }
}
