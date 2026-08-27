package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.ComprobanteRecepcionDonacion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ComprobanteRepository {

    private final List<ComprobanteRecepcionDonacion> comprobantes = new ArrayList<>();

    public void save(ComprobanteRecepcionDonacion comprobante) {
        this.comprobantes.add(comprobante);
    }

    public ComprobanteRecepcionDonacion findById(String id) {
        return this.comprobantes.stream()
                .filter(c -> c.getId() != null && c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<ComprobanteRecepcionDonacion> findAll() {
        return new ArrayList<>(this.comprobantes);
    }
}