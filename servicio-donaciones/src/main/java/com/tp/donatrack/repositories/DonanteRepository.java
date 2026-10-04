package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donante.Donante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonanteRepository extends JpaRepository<Donante, Long> {

    /**
     * Busca un donante por el valor de alguno de sus medios de contacto
     * (típicamente el email). Reemplaza la búsqueda previa sobre el Map en
     * memoria por una consulta sobre la tabla de medios de contacto.
     */
    @Query("SELECT d FROM Donante d JOIN d.persona p JOIN p.mediosDeContacto m WHERE m.valor = :valor")
    Optional<Donante> findByMedioDeContactoValor(@Param("valor") String valor);

    // ----- Métodos de compatibilidad con la API previa (en memoria) -----

    default Donante create(Donante donante) {
        return save(donante);
    }

    default void update(Donante donante) {
        save(donante);
    }

    /** Devolvía null si no existía; se preserva ese contrato. */
    default Donante find(String email) {
        return findByMedioDeContactoValor(email).orElse(null);
    }

    /** El método previo findById(Long) devolvía la entidad o null. */
    default Donante findByIdOrNull(Long id) {
        return findById(id).orElse(null);
    }

    default void clear() {
        deleteAll();
    }
}
