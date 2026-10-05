package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de {@link Donacion}, con búsquedas auxiliares sobre sus
 * segmentadas.
 */
@Repository
public interface DonacionRepository extends JpaRepository<Donacion, Long> {

    /** Devuelve las donaciones de un donante. */
    @Query("SELECT d FROM Donacion d WHERE d.donante.id = :donanteId")
    List<Donacion> findByDonanteId(@Param("donanteId") Long donanteId);

    /** Busca una donación segmentada por su id, devolviendo {@code null} si no existe. */
    default DonacionSegmentada findSegmentadaById(Long segmentadaId) {
        if (segmentadaId == null) {
            return null;
        }
        return findAll().stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(ds -> ds.getId() != null && ds.getId().equals(segmentadaId))
                .findFirst()
                .orElse(null);
    }

    /** Devuelve la donación que contiene la segmentada indicada, o {@code null} si no existe. */
    default Donacion findDonacionByDonacionesSegmentadaId(Long segmentadaId) {
        if (segmentadaId == null) {
            return null;
        }
        return findAll().stream()
                .filter(d -> d.getDonacionesSegmentadas().stream()
                        .anyMatch(ds -> ds.getId() != null && ds.getId().equals(segmentadaId)))
                .findFirst()
                .orElse(null);
    }

    /** Devuelve las donaciones segmentadas en depósito de un donante. */
    default List<DonacionSegmentada> findSegmentadasEnDepositoByDonanteId(Long donanteId) {
        return findByDonanteId(donanteId).stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(ds -> EstadoDonacionSegmentada.EN_DEPOSITO.equals(ds.getEstado()))
                .toList();
    }

    /** Elimina todas las donaciones. */
    default void clear() {
        deleteAll();
    }
}
