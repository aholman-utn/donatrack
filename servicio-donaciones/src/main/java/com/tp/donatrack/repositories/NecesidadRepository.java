package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class NecesidadRepository {

    private final List<NecesidadMaterial> necesidades = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public NecesidadMaterial create(NecesidadMaterial necesidad) {
        necesidad.setId(secuencia.getAndIncrement());
        this.necesidades.add(necesidad);
        return necesidad;
    }

    public Optional<NecesidadMaterial> findById(Long id) {
        return this.necesidades.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst();
    }

    public List<NecesidadMaterial> findByEntidadBeneficiariaId(Long entidadBeneficiariaId) {
        return this.necesidades.stream()
                .filter(n -> n.getEntidadBeneficiariaId().equals(entidadBeneficiariaId))
                .collect(Collectors.toList());
    }

    public NecesidadMaterial update(NecesidadMaterial necesidad) {
        // En memoria no hace falta actualizar la referencia si ya se modificó el objeto
        // pero simulamos un save
        return necesidad;
    }

    public void delete(NecesidadMaterial necesidad) {
        this.necesidades.remove(necesidad);
    }
    
    public void deleteById(Long id) {
        this.findById(id).ifPresent(this::delete);
    }

    public List<NecesidadMaterial> findAll() {
        return new ArrayList<>(this.necesidades);
    }
}
