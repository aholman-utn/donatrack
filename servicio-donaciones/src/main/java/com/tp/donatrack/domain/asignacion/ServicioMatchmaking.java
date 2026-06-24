package com.tp.donatrack.domain.asignacion;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que ejecuta el proceso de matchmaking para las donaciones en depósito.
 * Ejecuta múltiples algoritmos y filtra las coincidencias entre ellos.
 */
public class ServicioMatchmaking {

    private final List<AlgoritmoAsignacion> algoritmos;

    public ServicioMatchmaking(List<AlgoritmoAsignacion> algoritmos) {
        this.algoritmos = algoritmos;
    }

    public ServicioMatchmaking() {
        this.algoritmos = List.of(
                new AlgoritmoCompatibilidadSemantica(),
                new AlgoritmoPrioridadSubAtendidos()
        );
    }

    /**
     * Ejecuta el matchmaking para una donación en particular.
     * Retorna un ResultadoMatchmaking con las propuestas de cada algoritmo y las coincidencias.
     */
    public ResultadoMatchmaking ejecutar(DonacionSegmentada donacion, List<EntidadBeneficiaria> entidades) {
        if (donacion.getEstado() != EstadoDonacionSegmentada.EN_DEPOSITO) {
            throw new IllegalStateException("Solo se pueden asignar donaciones en estado EN_DEPOSITO");
        }

        List<EntidadBeneficiaria> resultadoCompatibilidad = algoritmos.get(0).rankear(donacion, entidades);
        List<EntidadBeneficiaria> resultadoSubAtendidos = algoritmos.get(1).rankear(donacion, entidades);

        List<EntidadBeneficiaria> coincidencias = resultadoCompatibilidad.stream()
                .filter(resultadoSubAtendidos::contains)
                .collect(Collectors.toList());

        return new ResultadoMatchmaking(coincidencias, resultadoCompatibilidad, resultadoSubAtendidos);
    }

    /**
     * Ejecuta matchmaking para todas las donaciones en depósito.
     */
    public List<ResultadoMatchmaking> ejecutarTodas(List<DonacionSegmentada> donaciones, List<EntidadBeneficiaria> entidades) {
        return donaciones.stream()
                .filter(d -> d.getEstado() == EstadoDonacionSegmentada.EN_DEPOSITO)
                .map(d -> ejecutar(d, entidades))
                .collect(Collectors.toList());
    }
}
