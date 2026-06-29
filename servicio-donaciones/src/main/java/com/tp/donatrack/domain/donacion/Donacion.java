package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.ClaveAgrupacion;

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
    private Integer id;
    private Donante donante;
    private String descripcion;
    private Date fechaIngreso;
    private List<Bien> bienes;
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
        this.bienes = bienes;
        this.donacionesSegmentadas = this.segmentar(bienes);
    }

    private List<DonacionSegmentada> segmentar(List<Bien> bienes) {       
        Map<ClaveAgrupacion, List<Bien>> agrupados = bienes.stream()
            .collect(Collectors.groupingBy(Bien::getClaveAgrupacion));

        return agrupados.entrySet().stream()
            .map(entry -> new DonacionSegmentada(
                entry.getValue().size(), 
                entry.getKey().subCategoria(),
                entry.getValue(),
                this.donante != null ? this.donante.getPersona().getId() : null
            ))
            .collect(Collectors.toList());
    }

    public EstadoDonacion getEstado() {
        boolean todasAsignadas = this.donacionesSegmentadas.stream()
                .allMatch(s -> s.getEstado() == EstadoDonacionSegmentada.ASIGNACION_REALIZADA
                        || s.getEstado() == EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR
                        || s.getEstado() == EstadoDonacionSegmentada.EN_TRASLADO
                        || s.getEstado() == EstadoDonacionSegmentada.ENTREGADA); 
        
        return todasAsignadas ? EstadoDonacion.ADJUDICADA : EstadoDonacion.PENDIENTE;
    }

    public Optional<DonacionSegmentada> buscarPorSubcategoria(SubCategoria sub) {
        return this.donacionesSegmentadas.stream()
                .filter(s -> s.getSubCategoria().equals(sub))
                .findFirst();
    }
}