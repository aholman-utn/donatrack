package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.Chofer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChoferRepository extends JpaRepository<Chofer, Long> {
    Optional<Chofer> findByDni(String dni);
}
