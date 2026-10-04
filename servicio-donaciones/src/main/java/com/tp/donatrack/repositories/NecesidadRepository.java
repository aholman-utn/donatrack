package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NecesidadRepository extends JpaRepository<NecesidadMaterial, Long> {

    List<NecesidadMaterial> findByEntidadBeneficiariaId(Long entidadBeneficiariaId);

    /**
     * Alias de {@link JpaRepository#save(Object)} para conservar la semántica de
     * creación que usaban los servicios antes de la migración a JPA.
     */
    default NecesidadMaterial create(NecesidadMaterial necesidad) {
        return save(necesidad);
    }

    /**
     * Alias de {@link JpaRepository#save(Object)} para conservar la semántica de
     * actualización previa.
     */
    default NecesidadMaterial update(NecesidadMaterial necesidad) {
        return save(necesidad);
    }
}
