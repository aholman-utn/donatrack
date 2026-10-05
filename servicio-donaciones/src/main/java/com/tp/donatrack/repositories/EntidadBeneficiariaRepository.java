package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de {@link EntidadBeneficiaria}.
 */
@Repository
public interface EntidadBeneficiariaRepository extends JpaRepository<EntidadBeneficiaria, Long> {

    /** Persiste la entidad beneficiaria. */
    default EntidadBeneficiaria create(EntidadBeneficiaria entidad) {
        return save(entidad);
    }

    /** Busca una entidad beneficiaria por su identificador, devolviendo {@code null} si no existe. */
    default EntidadBeneficiaria find(Long id) {
        if (id == null) {
            return null;
        }
        return findById(id).orElse(null);
    }
}
