package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.ComprobanteRecepcionDonacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de {@link ComprobanteRecepcionDonacion}, identificado por una
 * clave de tipo {@code String}.
 */
@Repository
public interface ComprobanteRepository extends JpaRepository<ComprobanteRecepcionDonacion, String> {
}
