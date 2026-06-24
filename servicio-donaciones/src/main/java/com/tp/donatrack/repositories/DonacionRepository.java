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
    private final AtomicInteger idsDonacion = new AtomicInteger(1);
    private final AtomicInteger idsSegmentada = new AtomicInteger(1);

    public Donacion save(Donacion donacion) {
        if (donacion.getId() == null) {
            donacion.setId(idsDonacion.getAndIncrement());
            this.donaciones.add(donacion);
        } else if (!this.donaciones.contains(donacion)) {
            this.donaciones.add(donacion);
        }
        for (DonacionSegmentada ds : donacion.getDonacionesSegmentadas()) {
            if (ds.getId() == null) {
                ds.setId(idsSegmentada.getAndIncrement());
            }
        }
        return donacion;
    }

    public void delete(Donacion donacion) {
        this.donaciones.remove(donacion);
    }

    public List<Donacion> findByDonanteId(Long donanteId) {
        return this.donaciones.stream()
                .filter(d -> d.getDonante() != null && d.getDonante().getPersona() != null && d.getDonante().getPersona().getId().equals(donanteId))
                .collect(Collectors.toList());
    }

    public Donacion findById(Integer id) {
        return this.donaciones.stream()
                .filter(d -> d.getId() != null && d.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Donacion> findAll() {
        return new ArrayList<>(this.donaciones);
    }

    public DonacionSegmentada findSegmentadaById(Integer segmentadaId) {
        return this.donaciones.stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(ds -> ds.getId() != null && ds.getId().equals(segmentadaId))
                .findFirst()
                .orElse(null);
    }

    public List<DonacionSegmentada> findSegmentadasEnDepositoByDonanteId(Long donanteId) {
        return findByDonanteId(donanteId).stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(ds -> ds.getEstado() == com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada.EN_DEPOSITO)
                .collect(Collectors.toList());
    }

    public void clear() { // creo que no se usa
        this.donaciones.clear();
        this.idsSegmentada.set(1);
    }
}
