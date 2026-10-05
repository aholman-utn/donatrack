package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donante.Donante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de {@link Donante}.
 */
@Repository
public interface DonanteRepository extends JpaRepository<Donante, Long> {

    /**
     * Busca un donante por el valor de alguno de sus medios de contacto
     * (típicamente el email), consultando la tabla de medios de contacto.
     */
    @Query("SELECT d FROM Donante d JOIN d.persona p JOIN p.mediosDeContacto m WHERE m.valor = :valor")
    Optional<Donante> findByMedioDeContactoValor(@Param("valor") String valor);

    /** Persiste el donante. */
    default Donante create(Donante donante) {
        return save(donante);
    }

    /** Actualiza el donante. */
    default void update(Donante donante) {
        save(donante);
    }

    /** Busca un donante por email, devolviendo {@code null} si no existe. */
    default Donante find(String email) {
        return findByMedioDeContactoValor(email).orElse(null);
    }

    /** Busca un donante por id, devolviendo {@code null} si no existe. */
    default Donante findByIdOrNull(Long id) {
        return findById(id).orElse(null);
    }

    /** Elimina todos los donantes. */
    default void clear() {
        deleteAll();
    }
}
