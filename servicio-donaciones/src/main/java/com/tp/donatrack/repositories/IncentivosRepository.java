package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.incentivos.PerfilIncentivosDonante;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class IncentivosRepository {

    private final List<PerfilIncentivosDonante> perfiles;

    public IncentivosRepository() {
        this.perfiles = new ArrayList<>();
    }

    public Optional<PerfilIncentivosDonante> findByDonanteId(Integer donanteId) {
        return this.perfiles.stream()
                .filter(p -> p.getDonanteId().equals(donanteId))
                .findFirst();
    }

    public PerfilIncentivosDonante save(PerfilIncentivosDonante perfil) {
        this.perfiles.removeIf(p -> p.getDonanteId().equals(perfil.getDonanteId()));
        this.perfiles.add(perfil);
        return perfil;
    }
}
