package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.persona.Persona;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class EntidadBeneficiariaRepository {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private final List<EntidadBeneficiaria> entidades;
    public EntidadBeneficiariaRepository() {
        this.entidades = new ArrayList<>();
    }

    public EntidadBeneficiaria create(EntidadBeneficiaria entidadBeneficiaria){
        if (entidadBeneficiaria.getDatosDeEntidad() != null && entidadBeneficiaria.getDatosDeEntidad().getId() == null) {
            entidadBeneficiaria.getDatosDeEntidad().setId(Persona.nextId());
            //entidadBeneficiaria.setEntidadBeneficiariaId(ID_GENERATOR.getAndIncrement());
        }
        this.entidades.add(entidadBeneficiaria);
        return entidadBeneficiaria;
    }

    public List<EntidadBeneficiaria> findAll() {return this.entidades;}

    public EntidadBeneficiaria find(Long id) {
        return this.entidades.stream()
                .filter(entidad -> entidad.getDatosDeEntidad() != null && entidad.getDatosDeEntidad().getId() != null && entidad.getDatosDeEntidad().getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void delete(EntidadBeneficiaria entidad) {entidades.remove(entidad);}

    public List<EntidadBeneficiaria> findAllById(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        return this.entidades.stream()
                .filter(entidad -> entidad.getDatosDeEntidad() != null
                        && entidad.getDatosDeEntidad().getId() != null
                        && ids.contains(entidad.getDatosDeEntidad().getId()))
                .toList();
    }

}
