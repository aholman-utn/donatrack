package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionEntregadaEvent;
import com.tp.donatrack.domain.donacion.DonacionEventPublisher;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.dtos.CrearDonacionRequest;
import com.tp.donatrack.dtos.DonacionEntregadaEventDTO;
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
    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final DonacionEventPublisher eventPublisher;

    public DonacionService(
            DonacionRepository donacionRepository,
            DonanteService donanteService,
            EntidadBeneficiariaService entidadBeneficiariaService,
            DonacionEventPublisher eventPublisher) {
        this.donacionRepository = donacionRepository;
        this.donanteService = donanteService;
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.eventPublisher = eventPublisher;
    }

    public Donacion registrarDonacion(CrearDonacionRequest request) {
        Donante donante = donanteService.buscarDonantePorId(request.getDonanteId());
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
    public void registrarEntrega(Long donacionSegmentadaId) {
        DonacionSegmentada segmentada = donacionRepository
                .findSegmentadaById(donacionSegmentadaId);

        if (segmentada == null) {
            throw new IllegalArgumentException("No se encontró la donación segmentada con ID: " + donacionSegmentadaId);
        }

        if (segmentada.getEntidadBeneficiariaAsignadaId() == null) {
            throw new IllegalStateException(
                    "La donación segmentada no tiene una entidad beneficiaria asignada. Ejecute el matchmaking primero.");
        }

        Long donanteId = segmentada.getDonanteId();

        Donante donante = this.donanteService.buscarDonantePorId(donanteId);

        segmentada.confirmarEntrega(segmentada.getEntidadBeneficiariaAsignadaId());
        this.notificarEntrega(segmentada);

        this.donanteService.registrarEntregaEnPerfil(donanteId, segmentada);

        if (eventPublisher != null) {
            DonacionEntregadaEventDTO donacionEntregada = new DonacionEntregadaEventDTO();
            donacionEntregada.setDonacionSegmentadaId(segmentada.getId());
            donacionEntregada.setProgreso(donante.getPerfil().getProgreso());
            donacionEntregada.setDonanteId(donante.getPersona().getId());
            donacionEntregada.setUltimaMisionId(donante.getPerfil().getMisionActualId());
            donacionEntregada.setCategoriaDonante(donante.getPerfil().getNivelDonante());
            donacionEntregada.setNombreDonante(donante.getNombreCompleto());
            eventPublisher.publicar(new DonacionEntregadaEvent(donacionEntregada));
        }
    }

    public void registrarEntregaFallida(Long donacionSegmentadaId, String motivo) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(donacionSegmentadaId);
        if (segmentada == null) {
            throw new IllegalArgumentException("No se encontró la donación segmentada con ID: " + donacionSegmentadaId);
        }
        segmentada.registrarEntregaFallida("Sistema",
                motivo != null ? motivo : "Entrega fallida reportada por logística");
    }

    public List<DonacionHistorialDTO> obtenerTodas() {
        return donacionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DonacionHistorialDTO obtenerPorId(Integer id) {
        Donacion donacion = donacionRepository.findById(id);
        if (donacion == null) {
            throw new IllegalArgumentException("No se encontró la donación con ID: " + id);
        }
        return mapToDTO(donacion);
    }

    private void notificarEntrega(DonacionSegmentada donacionSegmentada) {
        this.donanteService.notificarEntrega(donacionSegmentada.getDonanteId());
        this.entidadBeneficiariaService.notificarEntrega(donacionSegmentada.getEntidadBeneficiariaAsignadaId());
    }

    public DonacionHistorialDTO actualizarDonacion(Integer id,
            com.tp.donatrack.dtos.ActualizarDonacionRequest request) {
        Donacion donacion = donacionRepository.findById(id);
        if (donacion == null) {
            throw new IllegalArgumentException("No se encontró la donación con ID: " + id);
        }
        if (request.getDescripcion() != null) {
            donacion.setDescripcion(request.getDescripcion());
        }
        donacionRepository.save(donacion);
        return mapToDTO(donacion);
    }

    public void eliminarDonacion(Integer id) {
        Donacion donacion = donacionRepository.findById(id);
        if (donacion == null) {
            throw new IllegalArgumentException("No se encontró la donación con ID: " + id);
        }
        donacionRepository.delete(donacion);
    }

    public List<DonacionHistorialDTO> obtenerHistorialPorDonante(Long donanteId) {
        List<Donacion> donaciones = donacionRepository.findByDonanteId(donanteId);
        return donaciones.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private DonacionHistorialDTO mapToDTO(Donacion donacion) {
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
                .id(donacion.getId())
                .descripcion(donacion.getDescripcion())
                .fechaIngreso(donacion.getFechaIngreso())
                .estado(donacion.getEstado())
                .donacionesSegmentadas(segmentadas)
                .build();
    }
}
