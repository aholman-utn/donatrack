package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonacionRepository extends JpaRepository<Donacion, Long> {

    @Query("SELECT d FROM Donacion d WHERE d.donante.id = :donanteId")
    List<Donacion> findByDonanteId(@Param("donanteId") Long donanteId);

    // ----- Métodos de compatibilidad con la API previa (en memoria) -----

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

    default List<DonacionSegmentada> findSegmentadasEnDepositoByDonanteId(Long donanteId) {
        return findByDonanteId(donanteId).stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(ds -> EstadoDonacionSegmentada.EN_DEPOSITO.equals(ds.getEstado()))
                .toList();
    }

    default void clear() {
        deleteAll();
    }
}
