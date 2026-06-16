package com.tp.donatrack.domain.asignacion;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Asigna prioridad a organizaciones que hayan recibido menos donaciones en el último trimestre.
 * Favorece a las entidades sub-atendidas.
 */
public class AlgoritmoPrioridadSubAtendidos implements AlgoritmoAsignacion {

    private static final int MAX_RESULTADOS = 10;

    @Override
    public List<EntidadBeneficiaria> rankear(DonacionSegmentada donacion, List<EntidadBeneficiaria> entidades) {
        return entidades.stream()
                .filter(e -> tieneNecesidadActiva(e))
                .sorted(Comparator.comparingInt(this::totalDonacionesRecibidas))
                .limit(MAX_RESULTADOS)
                .collect(Collectors.toList());
    }

    private boolean tieneNecesidadActiva(EntidadBeneficiaria entidad) {
        return entidad.getNececidades().stream().anyMatch(NecesidadMaterial::activo);
    }

    /**
     * Cuenta la cantidad total de donaciones recibidas por la entidad (en todas sus necesidades).
     * A menor cantidad, mayor prioridad (va primero en el ranking).
     */
    private int totalDonacionesRecibidas(EntidadBeneficiaria entidad) {
        return entidad.getNececidades().stream()
                .mapToInt(n -> n.getDonaciones().size())
                .sum();
    }
}
