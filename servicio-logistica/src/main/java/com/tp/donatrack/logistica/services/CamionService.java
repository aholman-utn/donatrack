package com.tp.donatrack.logistica.services;

import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.repository.CamionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CamionService {
    private final CamionRepository camionRepository;

    public CamionService(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    public Camion registrarCamion(Camion camion) {
        return camionRepository.save(camion);
    }

    @Transactional(readOnly = true)
    public List<Camion> listarCamiones() {
        return camionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Camion buscarCamionPorId(Long id) {
        if (id == null) {
            return null;
        }
        return camionRepository.findById(id).orElse(null);
    }
}