package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donante.Donante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de {@link Donante}.
 */
@Repository
public interface DonanteRepository extends JpaRepository<Donante, Long> {

    /**
     * Recupera todos los donantes junto con su persona y dirección en una sola consulta,
     * mientras que mediosDeContacto y representantes se cargan por SUBSELECT para evitar N+1.
     */
    @Query("SELECT d FROM Donante d LEFT JOIN FETCH d.persona p LEFT JOIN FETCH p.direccion")
    List<Donante> findAllWithDetails();

    /**
     * Recupera únicamente los donantes cuya última interacción sea anterior a la fecha límite,
     * evitando cargar todo el padrón en memoria durante la ejecución del cron.
     */
    @Query("SELECT d FROM Donante d JOIN FETCH d.persona p WHERE p.fechaUltimaInteraccion IS NOT NULL AND p.fechaUltimaInteraccion < :fechaLimite")
    List<Donante> findDonantesInactivos(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Busca donantes por el valor de alguno de sus medios de contacto
     * (típicamente el email), consultando la tabla de medios de contacto.
     * Retorna una lista con DISTINCT para evitar NonUniqueResultException si
     * la persona tiene el valor repetido en medios predeterminados y secundarios.
     */
    @Query("SELECT DISTINCT d FROM Donante d JOIN d.persona p JOIN p.mediosDeContacto m WHERE m.valor = :valor")
    List<Donante> findByMedioDeContactoValor(@Param("valor") String valor);

    /**
     * Devuelve los donantes ordenados por total de donaciones exitosas (ranking desnormalizado).
     */
    @Query("SELECT d FROM Donante d ORDER BY d.perfil.totalDonacionesExitosas DESC")
    List<Donante> findTopDonantesRanking();

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
        List<Donante> donantes = findByMedioDeContactoValor(email);
        return donantes.isEmpty() ? null : donantes.get(0);
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
