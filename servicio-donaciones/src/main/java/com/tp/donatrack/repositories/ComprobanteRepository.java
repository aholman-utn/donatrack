package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.ComprobanteRecepcionDonacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComprobanteRepository extends JpaRepository<ComprobanteRecepcionDonacion, String> {
    // save(...), findById(String) y findAll() provienen de JpaRepository y
    // conservan las firmas que ya usaban los servicios.
}
