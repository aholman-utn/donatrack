package com.tp.donatrack.domain.entidad;

import java.util.ArrayList;
import java.util.List;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import com.tp.donatrack.domain.persona.Persona;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntidadBeneficiaria {

    public record Necesidad(Integer cantidad, SubCategoria subCategoria, List<Bien> bienes) {}

    private Persona persona;
    private List<Necesidad> necesidades = new ArrayList<>();
    private List<NecesidadMaterial> necesidadesMateriales = new ArrayList<>();

    public void agregarNecesidad(NecesidadMaterial necesidad) {
        this.necesidadesMateriales.add(necesidad);
    }

    public void removerNecesidad(NecesidadMaterial necesidad) {
        this.necesidadesMateriales.remove(necesidad);
    }

    public int getCantNecesidadesMateriales() {
        return this.necesidadesMateriales.size();
    }

    public void implementarDonacion(Donacion donacion) {
        for (DonacionSegmentada segmentada : donacion.getDonacionesSegmentadas()) {
            implementarDonacion(segmentada);
        }
    }

    public void implementarDonacion(DonacionSegmentada donacion) {
        Necesidad nuevaNecesidad = new Necesidad(
            donacion.getCantidad(),
            donacion.getSubCategoria(),
            donacion.getBienes()
        );

        if (this.necesidades.contains(nuevaNecesidad)) {
            this.necesidades.remove(nuevaNecesidad);
        } else {
            throw new RuntimeException("No existe esa necesidad en la lista de requerimientos");
        }
    }

    public Integer getCantNecesidades() {
        return this.necesidades.size();
    }
}
