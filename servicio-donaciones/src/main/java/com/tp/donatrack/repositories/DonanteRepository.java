package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donante.Donante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT DISTINCT d FROM Donante d JOIN d.persona p JOIN p.mediosDeContacto m "
            + "WHERE m.tipoNotificador = com.tp.commons.domain.notificador.TipoNotificador.EMAIL AND LOWER(m.valor) = LOWER(:email)")
    List<Donante> findByEmail(@Param("email") String email);

    /**
     * Devuelve los donantes ordenados por total de donaciones exitosas (ranking desnormalizado).
     */
    @Query("SELECT d FROM Donante d ORDER BY d.perfil.totalDonacionesExitosas DESC")
    List<Donante> findTopDonantesRanking();

    @Query("SELECT COUNT(d) FROM Donante d WHERE d.perfil.totalMisionesCompletadas > :misiones "
            + "OR (d.perfil.totalMisionesCompletadas = :misiones AND d.perfil.totalDonacionesExitosas > :entregas)")
    long contarDonantesMejorPosicionados(@Param("misiones") int misiones, @Param("entregas") int entregas);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Persona p SET p.fechaAvisoInactividad = :fecha WHERE p.id = :id "
            + "AND p.fechaUltimaInteraccion = :actividad "
            + "AND (p.fechaAvisoInactividad IS NULL OR p.fechaAvisoInactividad < :actividad)")
    int reservarAvisoInactividad(@Param("id") Long id, @Param("actividad") java.time.LocalDateTime actividad,
                                 @Param("fecha") java.time.LocalDateTime fecha);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Persona p SET p.fechaAvisoInactividad = NULL WHERE p.id = :id AND p.fechaAvisoInactividad = :fecha")
    void liberarAvisoInactividad(@Param("id") Long id, @Param("fecha") java.time.LocalDateTime fecha);

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
