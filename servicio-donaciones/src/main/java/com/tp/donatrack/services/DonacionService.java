package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificacion.TipoNotificacion;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
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
    private final NotificacionService notificacionService;
    private final PersonaService personaService;
    private final com.tp.donatrack.domain.donacion.DonacionEventPublisher eventPublisher;

    public DonacionService(
        DonacionRepository donacionRepository,
        DonanteService donanteService,
        EntidadBeneficiariaService entidadBeneficiariaService,
        NotificacionService notificacionService,
        PersonaService personaService,
        com.tp.donatrack.domain.donacion.DonacionEventPublisher eventPublisher
    ){
        this.donacionRepository = donacionRepository;
        this.donanteService = donanteService;
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.eventPublisher = eventPublisher;
        this.personaService = personaService;
        this.notificacionService = notificacionService;
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
            bienes
        );

        return donacionRepository.save(donacion);
    }

    public Donacion registrarDonacionPrueba(Integer donanteId) {
        com.tp.donatrack.domain.donante.Donante donante = donanteService.buscarDonantePorId(donanteId);
        if (donante == null) {
            throw new IllegalArgumentException("No se encontró el donante con ID: " + donanteId);
        }

        com.tp.donatrack.domain.bien.SubCategoria sub = new com.tp.donatrack.domain.bien.SubCategoria(
            com.tp.donatrack.domain.bien.Categoria.ALIMENTOS, 
            "Alimentos de Prueba", 
            com.tp.donatrack.domain.bien.Unidad.KG
        );

        com.tp.donatrack.domain.bien.BienPerecedero alimento = new com.tp.donatrack.domain.bien.BienPerecedero(
            "Alimento Prueba", 
            "Descripción Alimento Prueba", 
            "prueba.jpg", 
            sub, 
            new java.util.Date()
        );

        Donacion donacion = new Donacion(
            donante, 
            "Donación de prueba en Postman", 
            new java.util.Date(), 
            java.util.Arrays.asList(alimento)
        );

        return donacionRepository.save(donacion);
    }

    public void confirmarEntregaPrueba(Integer donanteId, Integer entidadBeneficiariaId) {
        List<Donacion> donaciones = donacionRepository.findByDonanteId(donanteId);
        if (donaciones.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron donaciones para el donante con ID: " + donanteId);
        }

        EntidadBeneficiaria entidad = entidadBeneficiariaService.buscarEntidad(entidadBeneficiariaId); 
        if (entidad == null) {
            throw new IllegalArgumentException("No se encontró la entidad beneficiaria con ID: " + entidadBeneficiariaId);
        }

        for (Donacion d : donaciones) {
            for (com.tp.donatrack.domain.donacion.DonacionSegmentada ds : d.getDonacionesSegmentadas()) {
                if (ds.getEstado() != com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada.ENTREGADA) {
                    ds.confirmarEntrega(entidadBeneficiariaId, eventPublisher);                    
                    this.notificarEntregaConfirmada(entidad, d.getDescripcion());

                    SubCategoria subCategoria = ds.getSubCategoria(); 
                    
                    donanteService.notificarDonacionAsignada(donanteId, subCategoria);
                }
            }

            donacionRepository.save(d);
        }
    }

    private void notificarEntregaConfirmada(EntidadBeneficiaria entidad, String descripcionDonacion) {
        com.tp.donatrack.domain.persona.Persona personaEntidad = entidad.getDatosDeEntidad();
        
        if (personaEntidad != null && personaEntidad.getMedioPredeterminado() != null && !personaEntidad.getMedioPredeterminado().isEmpty()) {
            
            java.util.Map.Entry<String, String> medio = personaEntidad.getMedioPredeterminado().entrySet().iterator().next();
            com.tp.donatrack.domain.notificador.TipoNotificador tipoNotificador = com.tp.donatrack.domain.notificador.TipoNotificador.valueOf(medio.getKey().toUpperCase());
            String contacto = medio.getValue();

            Notificacion notificacionEntrega = new Notificacion(
                "¡Entrega recibida con éxito!",
                "Se ha confirmado la recepción de la donación: " + descripcionDonacion,
                "Confirmación de Entrega",
                TipoNotificacion.ENTREGAS
            );

            notificacionService.notificar(
                notificacionEntrega, 
                tipoNotificador, 
                contacto
            );

            personaService.guardarNotificacion(personaEntidad, notificacionEntrega);
        }
    }

    public List<DonacionHistorialDTO> obtenerHistorialPorDonante(Integer donanteId) {
        List<Donacion> donaciones = donacionRepository.findByDonanteId(donanteId);
        
        return donaciones.stream().map(donacion -> {
            List<DonacionSegmentadaHistorialDTO> segmentadas = donacion.getDonacionesSegmentadas().stream()
                .map(ds -> DonacionSegmentadaHistorialDTO.builder()
                    .subCategoria(ds.getSubCategoria() != null ? ds.getSubCategoria().getDescripcion() : "Sin Categoría")
                    .cantidad(ds.getCantidad())
                    .estado(ds.getEstado())
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
