package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.entidad.Donante;
import com.tp.donatrack.domain.entidad.SubCategoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.bien.Bien;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;

@Getter
@Setter
public class Donacion {
    private Donante donante;
    private String descripcion;
    private Date fechaIngreso;
    private List<DonacionSegmentada> donacionesSegmentadas = new ArrayList<>();

    public Donacion(
        Donante donante, 
        String descripcion, 
        Date fechaIngreso
    ) {
        this.donante = donante;
        this.descripcion = descripcion;
        this.fechaIngreso = fechaIngreso;
    }

    public void segmentar(List<Bien> bienes) {
        if (bienes == null || bienes.isEmpty()) return;
        
        Map<SubCategoria, List<Bien>> agrupados = bienes.stream()
            .collect(Collectors.groupingBy(Bien::getSubCategoria));

        agrupados.forEach((subCat, listaDeBienes) -> {
            int cantidad = listaDeBienes.size();
            
            DonacionSegmentada nuevaDonacion = new DonacionSegmentada(
                cantidad, 
                subCat, 
                true,
                listaDeBienes
            );

            this.donacionesSegmentadas.add(nuevaDonacion);
        });
    }
}