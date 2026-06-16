package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.Donacion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DonacionRepository {

    private final List<Donacion> donaciones = new ArrayList<>();

    public Donacion save(Donacion donacion) {
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
}
