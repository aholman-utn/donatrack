package com.tp.donatrack.domain.donacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comprobantes_recepcion")
public class ComprobanteRecepcionDonacion {

    @Id
    @Column(name = "id_comprobante")
    private String id;

    @Column(name = "donacion_segmentada_id")
    private Long donacionSegmentadaId;

    @Column(name = "nombre_donante")
    private String nombreDonante;

    @Column(name = "nombre_entidad")
    private String nombreEntidad;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "detalles_logistica")
    private String detallesLogistica;

    public void generarId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}