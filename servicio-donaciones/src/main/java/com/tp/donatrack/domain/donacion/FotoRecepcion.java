package com.tp.donatrack.domain.donacion;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Foto que carga la entidad beneficiaria al confirmar la recepción de una donación.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fotos_recepcion")
public class FotoRecepcion {

    public static final int TAMANIO_MAXIMO = 2 * 1024 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Long id;

    @Column(name = "donacion_segmentada_id", nullable = false)
    private Long donacionSegmentadaId;

    /** Id de la entidad beneficiaria (tabla entidades_beneficiarias) que la cargó. */
    @Column(name = "entidad_beneficiaria_id", nullable = false)
    private Long entidadBeneficiariaId;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenido", nullable = false, length = TAMANIO_MAXIMO)
    private byte[] contenido;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    public FotoRecepcion(Long donacionSegmentadaId, Long entidadBeneficiariaId, String contentType, byte[] contenido) {
        this.donacionSegmentadaId = donacionSegmentadaId;
        this.entidadBeneficiariaId = entidadBeneficiariaId;
        this.contentType = contentType;
        this.contenido = contenido;
        this.fechaCarga = LocalDateTime.now();
    }
}
