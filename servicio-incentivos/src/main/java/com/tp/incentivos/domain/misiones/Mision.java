package com.tp.incentivos.domain.misiones;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.domain.incentivos.Insignia;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "misiones")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_mision", discriminatorType = DiscriminatorType.STRING)
public abstract class Mision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected int objetivo;

    @Convert(converter = NivelConverter.class)
    @Column(nullable = false, length = 30)
    protected Nivel nivel;

    protected String titulo;

    @Column(length = 500)
    protected String descripcion;

    protected int orden;

    /**
     * Retorna el identificador lógico de la misión para el donante (su orden dentro del nivel: 1, 2, 3..).
     */
    public Long getId() {
        return orden > 0 ? (long) orden : (id != null ? id : 1L);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDatabaseId() {
        return this.id;
    }

    public Insignia getInsigniaAsociada() {
        return new Insignia(this.titulo, this.descripcion);
    }

    public abstract double calcularNuevoProgreso(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas);

    public abstract boolean estaCumplida(EntregaDonacionDTO dto, IndicadoresDonanteDTO metricas);
}
