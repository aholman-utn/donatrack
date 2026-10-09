package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonacionSegmentadaRepository extends JpaRepository<DonacionSegmentada, Long> {
    List<DonacionSegmentada> findByDonanteIdAndEstado(Long donanteId, EstadoDonacionSegmentada estado);
}