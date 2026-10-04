package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntidadBeneficiariaRepository extends JpaRepository<EntidadBeneficiaria, Long> {

    // ----- Métodos de compatibilidad con la API previa (en memoria) -----

    default EntidadBeneficiaria create(EntidadBeneficiaria entidad) {
        return save(entidad);
    }

    /** El método previo find(Long) devolvía la entidad o null. */
    default EntidadBeneficiaria find(Long id) {
        if (id == null) {
            return null;
        }
        return findById(id).orElse(null);
    }
}
