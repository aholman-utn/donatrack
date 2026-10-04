package com.tp.donatrack.domain.bien;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@Entity
@Table(name = "bienes")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Bien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bien")
    protected Long id;

    @Column(name = "nombre")
    protected String nombre;

    @Column(name = "descripcion")
    protected String descripcion;

    @Column(name = "foto")
    protected String foto;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_subcategoria")
    protected SubCategoria subCategoria;

    protected Bien() {
    }

    public abstract Object getCriterioSegmentacion();

    public ClaveAgrupacion getClaveAgrupacion() {
        return new ClaveAgrupacion(this.subCategoria, this.getCriterioSegmentacion());
    }
}
