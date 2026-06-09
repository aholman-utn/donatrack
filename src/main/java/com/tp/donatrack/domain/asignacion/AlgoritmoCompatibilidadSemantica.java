package com.tp.donatrack.domain.asignacion;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analiza la correspondencia entre la subcategoría de la donación
 * y las necesidades declaradas por cada entidad beneficiaria.
 * Favorece a aquellas cuya demanda coincida de forma más precisa con la donación.
 */
public class AlgoritmoCompatibilidadSemantica implements AlgoritmoAsignacion {

    private static final int MAX_RESULTADOS = 10;

    @Override
    public List<EntidadBeneficiaria> rankear(DonacionSegmentada donacion, List<EntidadBeneficiaria> entidades) {
        return entidades.stream()
                .filter(e -> tieneNecesidadCompatible(e, donacion))
                .sorted(Comparator.comparingInt((EntidadBeneficiaria e) -> calcularPuntaje(e, donacion)).reversed())
                .limit(MAX_RESULTADOS)
                .collect(Collectors.toList());
    }

    private boolean tieneNecesidadCompatible(EntidadBeneficiaria entidad, DonacionSegmentada donacion) {
        return entidad.getNececidades().stream()
                .filter(NecesidadMaterial::activo)
                .anyMatch(n -> n.getSubCategoria().equals(donacion.getSubCategoria()));
    }

    /**
     * Puntaje basado en la cantidad faltante de la necesidad que coincide.
     * A mayor cantidad faltante, mayor prioridad (más necesita la donación).
     */
    private int calcularPuntaje(EntidadBeneficiaria entidad, DonacionSegmentada donacion) {
        return entidad.getNececidades().stream()
                .filter(NecesidadMaterial::activo)
                .filter(n -> n.getSubCategoria().equals(donacion.getSubCategoria()))
                .mapToInt(NecesidadMaterial::cantidadFaltanteDelPedido)
                .sum();
    }
}
