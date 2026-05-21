package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.bien.Bien;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

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
        Date fechaIngreso,
        List<Bien> bienes
    ) {
        if (bienes == null || bienes.isEmpty()) {
            throw new IllegalArgumentException("Una donación no puede crearse sin bienes.");
        }

        this.donante = donante;
        this.descripcion = descripcion;
        this.fechaIngreso = fechaIngreso;
        this.donacionesSegmentadas = this.segmentar(bienes);
    }

    private List<DonacionSegmentada> segmentar(List<Bien> bienes) {       
        Map<SubCategoria, List<Bien>> agrupados = bienes.stream()
            .collect(Collectors.groupingBy(Bien::getSubCategoria));

        List<DonacionSegmentada> segmentos = new ArrayList<>();
        agrupados.forEach((subCat, listaDeBienes) -> {
            int cantidad = listaDeBienes.size();
            
            DonacionSegmentada nuevaDonacion = new DonacionSegmentada(
                cantidad, 
                subCat, 
                true,
                listaDeBienes
            );

            segmentos.add(nuevaDonacion);
        });

        return segmentos;
    }

    public EstadoDonacion getEstado() {
        boolean todasAdjudicadas = this.donacionesSegmentadas.stream()
                .allMatch(s -> s.getEstado() == EstadoDonacionSegmentada.ADJUDICADA); 
        
        return todasAdjudicadas ? EstadoDonacion.ADJUDICADA : EstadoDonacion.PENDIENTE;
    }

    public Optional<DonacionSegmentada> buscarPorSubcategoria(SubCategoria sub) {
        return this.donacionesSegmentadas.stream()
                .filter(s -> s.getSubCategoria().equals(sub))
                .findFirst();
    }
}