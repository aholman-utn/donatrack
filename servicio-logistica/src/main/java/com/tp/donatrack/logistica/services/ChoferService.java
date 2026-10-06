package com.tp.donatrack.logistica.services;

import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.repository.ChoferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChoferService {
    private final ChoferRepository choferRepository;

    public ChoferService(ChoferRepository choferRepository) {
        this.choferRepository = choferRepository;
    }

    public Chofer registrarChofer(Chofer chofer) {
        return choferRepository.save(chofer);
    }

    @Transactional(readOnly = true)
    public List<Chofer> listarChoferes() {
        return choferRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Chofer buscarChoferPorId(Long id) {
        if (id == null) {
            return null;
        }
        return choferRepository.findById(id).orElse(null);
    }
}
