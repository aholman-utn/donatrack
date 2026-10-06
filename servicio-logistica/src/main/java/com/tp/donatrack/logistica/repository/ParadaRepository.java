package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.Parada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadaRepository extends JpaRepository<Parada, Long> {
    List<Parada> findByRutaIdOrderByOrdenAsc(Long rutaId);
}
