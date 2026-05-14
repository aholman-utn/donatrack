package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.entidad.Donante;
import com.tp.donatrack.domain.entidad.SubCategoria;
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
    }

    
    public void donar(EntidadBeneficiaria entidad){
        entidad.implementarDonacion(this);
    }
}
