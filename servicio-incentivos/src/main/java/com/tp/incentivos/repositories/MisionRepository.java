package com.tp.incentivos.repositories;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.incentivos.domain.misiones.Mision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MisionRepository extends JpaRepository<Mision, Long> {

    Optional<Mision> findByNivelAndId(Nivel nivel, Long id);

    Optional<Mision> findByNivelAndOrden(Nivel nivel, int orden);

    List<Mision> findByNivelOrderByOrdenAsc(Nivel nivel);

    default Optional<Mision> findById(Nivel nivel, Long misionId) {
        if (misionId == null) {
            return Optional.empty();
        }
        return findByNivelAndOrden(nivel, misionId.intValue())
                .or(() -> findByNivelAndId(nivel, misionId));
    }

    default Optional<Mision> findSiguiente(Nivel nivel, Long misionActualId) {
        return findById(nivel, misionActualId)
                .flatMap(actual -> findByNivelAndOrden(nivel, actual.getOrden() + 1));
    }

    default Optional<Mision> obtenerMisionInicial(Nivel nivel) {
        return findByNivelAndOrden(nivel, 1);
    }
}