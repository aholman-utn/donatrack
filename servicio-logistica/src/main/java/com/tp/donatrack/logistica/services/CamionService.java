package com.tp.donatrack.logistica.services;

import com.tp.donatrack.logistica.domain.Camion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CamionService {
    private final AtomicLong camionIdSeq = new AtomicLong(1);
    private final Map<Long, Camion> camiones = new ConcurrentHashMap<>();

    public Camion registrarCamion(Camion camion) {
        Long id = camionIdSeq.getAndIncrement();
        camion.setId(id);
        camiones.put(id, camion);
        return camion;
    }

    public List<Camion> listarCamiones() {
        return new ArrayList<>(camiones.values());
    }

    public Camion buscarCamionPorId(Long id) {
        if (id == null) {
            return null;
        }
        return camiones.get(id);
    }
}