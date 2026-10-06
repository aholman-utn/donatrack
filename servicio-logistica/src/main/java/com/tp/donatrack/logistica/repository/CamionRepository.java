package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    Optional<Camion> findByPatente(String patente);
}
