package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.DonacionEntregadaEvent;
import com.tp.donatrack.domain.incentivos.PerfilIncentivosDonante;
import com.tp.donatrack.dtos.PerfilIncentivosDTO;
import com.tp.donatrack.repositories.IncentivosRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IncentivosService {

    private final IncentivosRepository incentivosRepository;

    public IncentivosService(IncentivosRepository incentivosRepository) {
        this.incentivosRepository = incentivosRepository;
    }

    public PerfilIncentivosDTO obtenerPerfil(Integer donanteId) {
        Optional<PerfilIncentivosDonante> perfilOpt = incentivosRepository.findByDonanteId(donanteId);
        
        PerfilIncentivosDonante perfil = perfilOpt.orElseGet(() -> 
            PerfilIncentivosDonante.builder()
                .donanteId(donanteId)
                .totalesHistoricosDonaciones(0)
                .organizacionesAyudadasCount(0)
                .posicionRanking(0)
                .build()
        );

        return PerfilIncentivosDTO.builder()
            .donanteId(perfil.getDonanteId())
            .totalesHistoricosDonaciones(perfil.getTotalesHistoricosDonaciones())
            .organizacionesAyudadasCount(perfil.getOrganizacionesAyudadasCount())
            .posicionRanking(perfil.getPosicionRanking())
            .build();
    }

    @EventListener
    public void actualizarMetricasPorEntrega(DonacionEntregadaEvent event) {
        if (event.donanteId() == null) {
            return; // No hay donante asociado
        }

        // Buscar o crear perfil
        PerfilIncentivosDonante perfil = incentivosRepository.findByDonanteId(event.donanteId())
                .orElse(PerfilIncentivosDonante.builder()
                        .donanteId(event.donanteId())
                        .totalesHistoricosDonaciones(0)
                        .organizacionesAyudadasCount(0)
                        .posicionRanking(0)
                        .build());

        // Actualizar métricas
        perfil.incrementarDonaciones();
        
        // Asumimos un incremento simple por cada donación entregada para organizaciones
        // En una implementación real, se validaría si la organización es nueva para el donante.
        perfil.incrementarOrganizaciones();

        // Persistir (en este caso en memoria, pero el método save asegura semántica)
        incentivosRepository.save(perfil);
    }
}
