package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de {@link NecesidadMaterial}.
 */
@Repository
public interface NecesidadRepository extends JpaRepository<NecesidadMaterial, Long> {

    /** Devuelve las necesidades de una entidad beneficiaria. */
    List<NecesidadMaterial> findByEntidadBeneficiariaId(Long entidadBeneficiariaId);

    /** Persiste la necesidad. */
    default NecesidadMaterial create(NecesidadMaterial necesidad) {
        return save(necesidad);
    }

    /** Actualiza la necesidad. */
    default NecesidadMaterial update(NecesidadMaterial necesidad) {
        return save(necesidad);
    }
}
