package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.ClaveAgrupacion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
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
@Entity
@Table(name = "donaciones")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_donacion")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_donante")
    private Donante donante;

    @Column(name = "descripcion")
    private String descripcion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_ingreso")
    private Date fechaIngreso;

    // Los bienes se agrupan en segmentos; en el DER el Bien referencia a la
    // donación. Se mantiene la lista para la lógica de segmentación.
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_donacion")
    private List<Bien> bienes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_donacion")
    private List<DonacionSegmentada> donacionesSegmentadas = new ArrayList<>();

    protected Donacion() {
    }

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
                .allMatch(s -> s.getEstado() != null && s.getEstado().isAsignada()); 
        
        return todasAsignadas ? EstadoDonacion.ADJUDICADA : EstadoDonacion.PENDIENTE;
    }

    public Optional<DonacionSegmentada> buscarPorSubcategoria(SubCategoria sub) {
        return this.donacionesSegmentadas.stream()
                .filter(s -> s.getSubCategoria().equals(sub))
                .findFirst();
    }
}