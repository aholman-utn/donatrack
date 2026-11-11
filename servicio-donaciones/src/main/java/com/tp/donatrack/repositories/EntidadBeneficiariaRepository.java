package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de {@link EntidadBeneficiaria}.
 */
@Repository
public interface EntidadBeneficiariaRepository extends JpaRepository<EntidadBeneficiaria, Long> {

    boolean existsByDatosDeEntidadCuit(String cuit);
    java.util.Optional<EntidadBeneficiaria> findByEmailAccesoIgnoreCase(String email);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT e FROM EntidadBeneficiaria e JOIN e.datosDeEntidad p JOIN p.mediosDeContacto m WHERE m.tipoNotificador = com.tp.commons.domain.notificador.TipoNotificador.EMAIL AND lower(m.valor) = lower(:email)")
    List<EntidadBeneficiaria> findByCorreoContacto(@org.springframework.data.repository.query.Param("email") String email);

    /** Devuelve las entidades ordenadas por total de donaciones recibidas (prioridad a sub-atendidos). */
    List<EntidadBeneficiaria> findAllByOrderByTotalDonacionesRecibidasAsc();

    /** Busca por id de la persona jurídica, que es lo que guarda DonacionSegmentada.entidadBeneficiariaAsignadaId. */
    List<EntidadBeneficiaria> findAllByDatosDeEntidadIdIn(java.util.Collection<Long> personaJuridicaIds);

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
