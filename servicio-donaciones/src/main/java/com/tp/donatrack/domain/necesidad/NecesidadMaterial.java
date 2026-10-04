package com.tp.donatrack.domain.necesidad;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "necesidades")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class NecesidadMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_necesidad_material")
    private Long id;

    @Column(name = "entidad_beneficiaria_id")
    private Long entidadBeneficiariaId;

    // Agregado DonacionSegmentada aún no persistido vía JPA: se mantiene en memoria.
    @Transient
    private List<DonacionSegmentada> donaciones = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_subcategoria")
    private SubCategoria subCategoria;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_del_pedido")
    private Date fechaDelPedido;

    @Column(name = "cantidad_objetivo")
    private int cantidadObjetivo;

    @Column(name = "cantidad_recibida")
    private int cantidadRecibida = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoNecesidad estado;

    // Requerido por JPA.
    protected NecesidadMaterial() {
    }

    public NecesidadMaterial(SubCategoria subCategoria, int cantidadObjetivo, Date fechaDelPedido) {
        this.subCategoria = subCategoria;
        this.cantidadObjetivo = cantidadObjetivo;
        this.fechaDelPedido = fechaDelPedido;
        this.estado = EstadoNecesidad.ACTIVO;
    }

    public boolean activo() {
        return this.estado == EstadoNecesidad.ACTIVO;
    }

    public int cantidadFaltanteDelPedido() {
        return Math.max(cantidadObjetivo - cantidadRecibida, 0);
    }

    public void recibirDonacion(DonacionSegmentada donacion) {
        this.donaciones.add(donacion);
        recibirBienes(donacion.getCantidad());
    }

    public void recibirBienes(int cantidadRecibida) {
        this.cantidadRecibida += cantidadRecibida;
        if (cantidadFaltanteDelPedido() == 0)
            this.estado = EstadoNecesidad.SATISFECHO;
    }

    public void finalizarNecesidad() {
        if (cantidadFaltanteDelPedido() == 0) {
            this.estado = EstadoNecesidad.SATISFECHO;
        } else {
            this.estado = EstadoNecesidad.INSATISFECHO;
        }
    }
}
