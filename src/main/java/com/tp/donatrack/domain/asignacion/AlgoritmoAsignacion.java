package com.tp.donatrack.domain.asignacion;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;

import java.util.List;

/**
 * Interface Strategy para los algoritmos de asignación de donaciones.
 * Cada implementación aplica un criterio distinto para rankear entidades beneficiarias.
 */
public interface AlgoritmoAsignacion {

    /**
     * Genera un ranking de hasta 10 entidades beneficiarias candidatas para recibir la donación.
     * Las entidades se ordenan de mayor a menor correspondencia según el criterio del algoritmo.
     */
    List<EntidadBeneficiaria> rankear(DonacionSegmentada donacion, List<EntidadBeneficiaria> entidades);
}
