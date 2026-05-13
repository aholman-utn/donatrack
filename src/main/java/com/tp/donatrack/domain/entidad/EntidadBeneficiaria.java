package com.tp.donatrack.domain.entidad;

import java.util.ArrayList;
import java.util.List;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.persona.Persona;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntidadBeneficiaria {
    
    public record Necesidad(Integer cantidad, SubCategoria subCategoria, List<Bien> bienes) {}    
    
    private Persona persona;
    private List<Necesidad> necesidades = new ArrayList<>();

    public void implementarDonacion(Donacion donacion) {
        Necesidad nuevaNecesidad = new Necesidad(
            donacion.getCantidad(), 
            donacion.getSubCategoria(), 
            donacion.getBienes()
        );
        
        this.necesidades.remove(nuevaNecesidad);
    }
    public void implementarDonacion(DonacionSegmentada donacion) {
        Necesidad nuevaNecesidad = new Necesidad(
            donacion.getCantidad(), 
            donacion.getSubCategoria(), 
            donacion.getBienes()
        );
        
        boolean existeNecesidad = this.necesidades.stream().anyMatch(necesidad -> 
            necesidad.cantidad().equals(nuevaNecesidad.cantidad()) && 
            necesidad.subCategoria().equals(nuevaNecesidad.subCategoria())
        );
        
         if(this.necesidades.contains(nuevaNecesidad)){
            this.necesidades.remove(nuevaNecesidad);    
        }else{
            throw new RuntimeException("No existe esa necesidad en la lista de requerimientos");
        }

    public Integer getCantNecesidades(){
        return this.necesidades.size();
    }
}
