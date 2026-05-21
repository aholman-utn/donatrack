package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.bien.Bien;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class DonacionSegmentada {
    private int cantidad;
    private SubCategoria subCategoria;
    private Boolean disponible;
    private List<Bien> bienes;
    public EstadoDonacionSegmentada estado;

    public DonacionSegmentada(
        int cantidad, 
        SubCategoria subCategoria, 
        Boolean disponible, 
        List<Bien> bienes
    ) {
        this.cantidad = cantidad;
        this.subCategoria = subCategoria;
        this.disponible = disponible;
        this.bienes = bienes;
        this.estado = EstadoDonacionSegmentada.PENDIENTE;
    }
    
    public void donar(EntidadBeneficiaria entidad){
        try {
            entidad.implementarDonacion(this);
            this.setEstado(EstadoDonacionSegmentada.ADJUDICADA);            
        } catch (RuntimeException e) {
            System.err.println("Error al procesar la donación: " + e.getMessage());         
        }
    }
}
