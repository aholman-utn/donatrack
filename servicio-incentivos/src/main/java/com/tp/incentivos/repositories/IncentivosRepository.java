package com.tp.incentivos.repositories;

import com.tp.incentivos.domain.PerfilIncentivosDonante;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class IncentivosRepository {
    private final Map<Long, PerfilIncentivosDonante> perfiles = new ConcurrentHashMap<>();

    public Optional<PerfilIncentivosDonante> findByDonanteId(Long donanteId) {
        return Optional.ofNullable(perfiles.get(donanteId));
    }

    public PerfilIncentivosDonante save(PerfilIncentivosDonante perfil) {
        perfiles.put(perfil.getDonanteId(), perfil);
        return perfil;
    }
}
