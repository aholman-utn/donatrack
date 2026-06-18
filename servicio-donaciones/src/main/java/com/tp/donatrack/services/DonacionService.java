package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.dtos.DonacionHistorialDTO;
import com.tp.donatrack.dtos.DonacionSegmentadaHistorialDTO;
import com.tp.donatrack.repositories.DonacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonacionService {

    private final DonacionRepository donacionRepository;
    private final DonanteService donanteService;
    private final com.tp.donatrack.domain.donacion.DonacionEventPublisher eventPublisher;

    public DonacionService(
            DonacionRepository donacionRepository,
            DonanteService donanteService,
            com.tp.donatrack.domain.donacion.DonacionEventPublisher eventPublisher) {
        this.donacionRepository = donacionRepository;
        this.donanteService = donanteService;
        this.eventPublisher = eventPublisher;
    }

    public Donacion registrarDonacion(com.tp.donatrack.dtos.CrearDonacionRequest request) {
        com.tp.donatrack.domain.donante.Donante donante = donanteService.buscarDonantePorId(request.getDonanteId());
        if (donante == null) {
            throw new IllegalArgumentException("No se encontró el donante con ID: " + request.getDonanteId());
        }

        List<com.tp.donatrack.domain.bien.Bien> bienes = request.toDomainBienes();
        Donacion donacion = new Donacion(
                donante,
                request.getDescripcion(),
                new java.util.Date(),
                bienes);

        return donacionRepository.save(donacion);
    }

    /**
     * Confirma la entrega de una donación segmentada específica.
     * Utiliza la entidad beneficiaria que fue asignada durante el matchmaking.
     */
    public void registrarEntrega(Integer donacionSegmentadaId) {
        com.tp.donatrack.domain.donacion.DonacionSegmentada segmentada =
                donacionRepository.findSegmentadaById(donacionSegmentadaId);

        if (segmentada == null) {
            throw new IllegalArgumentException("No se encontró la donación segmentada con ID: " + donacionSegmentadaId);
        }

        if (segmentada.getEntidadBeneficiariaAsignadaId() == null) {
            throw new IllegalStateException("La donación segmentada no tiene una entidad beneficiaria asignada. Ejecute el matchmaking primero.");
        }

        segmentada.confirmarEntrega(segmentada.getEntidadBeneficiariaAsignadaId(), eventPublisher);
    }

    public List<DonacionHistorialDTO> obtenerHistorialPorDonante(Integer donanteId) {
        List<Donacion> donaciones = donacionRepository.findByDonanteId(donanteId);

        return donaciones.stream().map(donacion -> {
            List<DonacionSegmentadaHistorialDTO> segmentadas = donacion.getDonacionesSegmentadas().stream()
                    .map(ds -> DonacionSegmentadaHistorialDTO.builder()
                            .id(ds.getId())
                            .subCategoria(ds.getSubCategoria() != null ? ds.getSubCategoria().getDescripcion()
                                    : "Sin Categoría")
                            .cantidad(ds.getCantidad())
                            .estado(ds.getEstado())
                            .entidadBeneficiariaAsignadaId(ds.getEntidadBeneficiariaAsignadaId())
                            .build())
                    .collect(Collectors.toList());

            return DonacionHistorialDTO.builder()
                    .descripcion(donacion.getDescripcion())
                    .fechaIngreso(donacion.getFechaIngreso())
                    .estado(donacion.getEstado())
                    .donacionesSegmentadas(segmentadas)
                    .build();
        }).collect(Collectors.toList());
    }
}
