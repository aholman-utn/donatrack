package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class DonacionRepository {

    private final List<Donacion> donaciones = new ArrayList<>();
    private final AtomicInteger secuenciaSegmentada = new AtomicInteger(1);

    public Donacion save(Donacion donacion) {
        for (DonacionSegmentada ds : donacion.getDonacionesSegmentadas()) {
            if (ds.getId() == null) {
                ds.setId(secuenciaSegmentada.getAndIncrement());
            }
        }
        this.donaciones.add(donacion);
        return donacion;
    }

    public List<Donacion> findByDonanteId(Integer donanteId) {
        return this.donaciones.stream()
                .filter(d -> d.getDonante() != null && d.getDonante().getId().equals(donanteId))
                .collect(Collectors.toList());
    }

    public List<Donacion> findAll() {
        return new ArrayList<>(this.donaciones);
    }

    /**
     * Busca una donación segmentada por su ID a través de todas las donaciones.
     */
    public DonacionSegmentada findSegmentadaById(Integer segmentadaId) {
        return this.donaciones.stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(ds -> ds.getId() != null && ds.getId().equals(segmentadaId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retorna todas las donaciones segmentadas en estado EN_DEPOSITO de un donante.
     */
    public List<DonacionSegmentada> findSegmentadasEnDepositoByDonanteId(Integer donanteId) {
        return findByDonanteId(donanteId).stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(ds -> ds.getEstado() == com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada.EN_DEPOSITO)
                .collect(Collectors.toList());
    }

    public void clear() {
        this.donaciones.clear();
        this.secuenciaSegmentada.set(1);
    }
}
