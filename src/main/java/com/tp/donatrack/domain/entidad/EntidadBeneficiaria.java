package com.tp.donatrack.domain.entidad;

import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.donacion.Donacion;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class EntidadBeneficiaria {
    
    public record Necesidad(Integer cantidad, SubCategoria subCategoria, List<Bien> bienes) {}    
    
    private Persona persona;
    private List<Necesidad> necesidades = new ArrayList<>();


    /*
    public void implementarDonacion(Donacion donacion) {
        Necesidad nuevaNecesidad = new Necesidad(
            donacion.getCantidad(), 
            donacion.getSubCategoria(), 
            donacion.getBienes()
        );
        
        this.necesidades.remove(nuevaNecesidad);
    }
    */
}