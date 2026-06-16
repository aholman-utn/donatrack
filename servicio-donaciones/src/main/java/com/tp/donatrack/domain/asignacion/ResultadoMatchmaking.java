package com.tp.donatrack.domain.asignacion;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import lombok.Getter;

import java.util.List;

/**
 * Resultado del proceso de matchmaking para una donación.
 * Contiene las entidades propuestas por cada algoritmo y las coincidencias entre ambos.
 */
@Getter
public class ResultadoMatchmaking {

    private final List<EntidadBeneficiaria> coincidencias;
    private final List<EntidadBeneficiaria> resultadoCompatibilidad;
    private final List<EntidadBeneficiaria> resultadoSubAtendidos;
    private final boolean huboCoincidencias;

    public ResultadoMatchmaking(
            List<EntidadBeneficiaria> coincidencias,
            List<EntidadBeneficiaria> resultadoCompatibilidad,
            List<EntidadBeneficiaria> resultadoSubAtendidos
    ) {
        this.coincidencias = coincidencias;
        this.resultadoCompatibilidad = resultadoCompatibilidad;
        this.resultadoSubAtendidos = resultadoSubAtendidos;
        this.huboCoincidencias = !coincidencias.isEmpty();
    }

    /**
     * Si hubo coincidencias entre ambos algoritmos, retorna esas.
     * Si no, retorna ambas listas combinadas para que el admin decida.
     */
    public List<EntidadBeneficiaria> getEntidadesPropuestas() {
        return huboCoincidencias ? coincidencias : resultadoCompatibilidad;
    }
}
