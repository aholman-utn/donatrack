package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.Ruta;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class RutaRepository {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private final List<Ruta> rutas;

    public RutaRepository() {
        this.rutas = new ArrayList<>();
    }

    public Ruta save(Ruta ruta) {
        if (ruta.getId() == null) {
            ruta.setId(ID_GENERATOR.getAndIncrement());
            this.rutas.add(ruta);
        } else {
            this.rutas.removeIf(r -> r.getId().equals(ruta.getId()));
            this.rutas.add(ruta);
        }
        return ruta;
    }

    public List<Ruta> saveAll(List<Ruta> nuevasRutas) {
        if (nuevasRutas != null) {
            nuevasRutas.forEach(this::save);
        }
        return nuevasRutas;
    }

    public Ruta findById(Long id) {
        return this.rutas.stream()
                .filter(ruta -> ruta.getId() != null && ruta.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Ruta> findAll() {
        return new ArrayList<>(this.rutas);
    }

    public void delete(Ruta ruta) {
        this.rutas.removeIf(r -> r.getId().equals(ruta.getId()));
    }
}