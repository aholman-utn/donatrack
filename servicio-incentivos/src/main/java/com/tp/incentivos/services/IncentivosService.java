package com.tp.incentivos.services;

import com.tp.incentivos.domain.PerfilIncentivosDonante;
import com.tp.incentivos.dtos.PerfilIncentivosDTO;
import com.tp.incentivos.repositories.IncentivosRepository;
import org.springframework.stereotype.Service;

@Service
public class IncentivosService {
    private final IncentivosRepository repository;

    public IncentivosService(IncentivosRepository repository) {
        this.repository = repository;
    }

    public void procesarNuevaEntrega(Long donanteId, Long entidadBeneficiariaId) {
        PerfilIncentivosDonante perfil = repository.findByDonanteId(donanteId)
            .orElseGet(() -> PerfilIncentivosDonante.builder()
                .donanteId(donanteId)
                .build()
            );

        perfil.registrarEntrega(entidadBeneficiariaId);
        repository.save(perfil);

        // TODO: Hook para evaluar cumplimiento de Misiones (Racha, Completitud, etc.)
    }

    public PerfilIncentivosDTO obtenerPerfil(Long donanteId) {
        PerfilIncentivosDonante perfil = repository.findByDonanteId(donanteId)
            .orElseGet(() -> PerfilIncentivosDonante.builder()
                .donanteId(donanteId)
                .build()
            );

        return PerfilIncentivosDTO.builder()
            .donanteId(perfil.getDonanteId())
            .totalDonacionesExitosas(perfil.getTotalDonacionesExitosas())
            .entidadesAyudadasIds(perfil.getEntidadesAyudadasIds())
            .entidadesAyudadasCount(perfil.getEntidadesAyudadasIds().size())
            .build();
    }
}
