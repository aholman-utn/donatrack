package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class EntidadBeneficiariaRepository {
    private final List<EntidadBeneficiaria> entidades;
    public EntidadBeneficiariaRepository() {
        this.entidades = new ArrayList<>();
    }

    private final AtomicInteger secuencia = new AtomicInteger(1);

    public EntidadBeneficiaria create(EntidadBeneficiaria entidadBeneficiaria){
        entidadBeneficiaria.setId(secuencia.getAndIncrement());
        this.entidades.add(entidadBeneficiaria);
        return entidadBeneficiaria;
    }

    public List<EntidadBeneficiaria> findAll() {return this.entidades;}

    public EntidadBeneficiaria find(Integer id) {
        return this.entidades.stream().filter(entidad -> entidad.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void delete(EntidadBeneficiaria entidad) {entidades.remove(entidad);}

}
