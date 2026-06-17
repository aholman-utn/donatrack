package com.tp.incentivos.repositories;

import com.tp.incentivos.domain.PerfilIncentivosDonante;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

@Repository
public class IncentivosRepository {
    private final List<PerfilIncentivosDonante> perfiles = new ArrayList<>();

    public PerfilIncentivosDonante findByDonanteId(Integer donanteId) {
        return this.perfiles.stream().filter(p -> p.getDonanteId().equals(donanteId)).findFirst().orElse(null);
    }

    public PerfilIncentivosDonante create(PerfilIncentivosDonante perfil) {
        this.perfiles.add(perfil);
        return perfil;
    }

    public Collection<PerfilIncentivosDonante> findAll() {
        return perfiles;
    }
}
