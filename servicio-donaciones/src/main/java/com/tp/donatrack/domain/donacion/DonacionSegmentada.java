package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.donacion.estado.EnDeposito;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "donaciones_segmentadas")
public class DonacionSegmentada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_donacion_segmentada")
    private Long id;

    @Column(name = "cantidad")
    private int cantidad; //300 kg de fideos, 200lt de leche...etc

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_subcategoria")
    private SubCategoria subCategoria;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_donacion_segmentada")
    private List<Bien> bienes;

    @Convert(converter = EstadoDonacionSegmentadaConverter.class)
    @Column(name = "estado_donacion")
    private EstadoDonacionSegmentada estado;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_donacion_segmentada")
    @OrderColumn(name = "orden_historial")
    private List<EventoTrazabilidad> historial = new ArrayList<>();

    @Column(name = "donante_id")
    private Long donanteId;

    @Column(name = "entidad_beneficiaria_asignada_id")
    private Long entidadBeneficiariaAsignadaId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_comprobante")
    private ComprobanteRecepcionDonacion comprobanteRecepcionDonacion;

    // Requerido por JPA.
    protected DonacionSegmentada() {
    }

    public DonacionSegmentada(
            int cantidad,
            SubCategoria subCategoria,
            List<Bien> bienes) {
        this(cantidad, subCategoria, bienes, null);
    }

    public DonacionSegmentada(
            int cantidad,
            SubCategoria subCategoria,
            List<Bien> bienes,
            Long donanteId) {
        this.cantidad = cantidad;
        this.subCategoria = subCategoria;
        this.bienes = bienes;
        this.donanteId = donanteId;
        this.estado = new EnDeposito();
        registrarEvento(null, this.estado, "Administrador",
                "Donación registrada e ingresada al depósito");
    }

    public void transicionar(EstadoDonacionSegmentada nuevoEstado, String actor, String descripcion) {
        EstadoDonacionSegmentada anterior = this.estado;
        this.estado = nuevoEstado;
        registrarEvento(anterior, nuevoEstado, actor, descripcion);
    }

    public void asignar(EntidadBeneficiaria entidad, String actor) {
        this.estado.asignar(this, entidad, actor);
    }

    /** @deprecated Usar asignar(entidad, actor) para trazabilidad completa */
    @Deprecated
    public void donar(EntidadBeneficiaria entidad) {
        asignar(entidad, "Administrador");
    }

    public void listarParaEntrega(String actor) {
        this.estado.listarParaEntrega(this, actor);
    }

    public void iniciarTraslado(String actor) {
        this.estado.iniciarTraslado(this, actor);
    }

    public void confirmarEntrega(Long entidadBeneficiariaId) {
        this.estado.confirmarEntrega(this, entidadBeneficiariaId);
    }

    public void registrarEntregaFallida(String actor, String justificacion) {
        this.estado.registrarEntregaFallida(this, actor, justificacion);
    }

    public void marcarVencida(String actor) {
        this.estado.marcarVencida(this, actor);
    }

    public void registrarLlegadaADestino(String actor) {
        this.estado.registrarLlegadaADestino(this, actor);
    }

    public List<EventoTrazabilidad> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public EventoTrazabilidad getUltimoEvento() {
        if (historial.isEmpty())
            return null;
        return historial.get(historial.size() - 1);
    }

    public boolean transicionPosible(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo) {
        return anterior != null && anterior.puedeTransicionarA(nuevo);
    }

    public void registrarEventoTrazabilidad(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo, String actor, String descripcion) {
        registrarEvento(anterior, nuevo, actor, descripcion);
    }

    private void registrarEvento(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo, String actor,
            String descripcion) {
        historial.add(new EventoTrazabilidad(anterior, nuevo, actor, descripcion));
    }
}
