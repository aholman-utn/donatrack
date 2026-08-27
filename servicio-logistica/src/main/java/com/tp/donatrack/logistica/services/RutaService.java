package com.tp.donatrack.logistica.services;

import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import com.tp.donatrack.logistica.domain.*;
import com.tp.donatrack.logistica.domain.planificacion.Planificacion;
import com.tp.donatrack.logistica.repository.LogisticaEventRepository;
import com.tp.donatrack.logistica.repository.RutaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RutaService {
    private static final Logger logger = LoggerFactory.getLogger(RutaService.class);

    private final LogisticaEventRepository eventRepository;
    private final RutaRepository rutaRepository;
    private final Planificacion planificacion;

    private final CamionService camionService;
    private final ChoferService choferService;
    private final EnviosService enviosService;

    public RutaService(
            LogisticaEventRepository eventRepository,
            RutaRepository rutaRepository,
            Planificacion planificacion,
            CamionService camionService,
            ChoferService choferService,
            @Lazy EnviosService enviosService // Lazy es para evitar espera circular.
    ) {
        this.eventRepository = eventRepository;
        this.rutaRepository = rutaRepository;
        this.planificacion = planificacion;
        this.camionService = camionService;
        this.choferService = choferService;
        this.enviosService = enviosService;
    }

    public void planificarLote(List<DonacionSegmentadaListaParaEntregarALogisticaDTO> loteDonaciones) {
        if (loteDonaciones == null || loteDonaciones.isEmpty()) {
            logger.warn("Se recibió un lote de planificación vacío.");
            return;
        }

        List<Camion> camionesDisponibles = camionService.listarCamiones();
        List<Chofer> choferesDisponibles = choferService.listarChoferes();

        if (camionesDisponibles.isEmpty() || choferesDisponibles.isEmpty()) {
            logger.error("No se puede planificar: Faltan camiones o choferes en el sistema.");
            throw new IllegalStateException("No hay camiones o choferes registrados para armar las rutas.");
        }

        logger.info("Planificando {} donaciones..", loteDonaciones.size());

        List<Ruta> nuevasRutas = planificacion.planificar(
                loteDonaciones,
                camionesDisponibles,
                choferesDisponibles
        );

        for (Ruta ruta : nuevasRutas) {
            if (ruta.getParadas() != null) {
                for (Parada parada : ruta.getParadas()) {
                    List<Long> enviosRealesIds = new ArrayList<>();

                    if (parada.getEnviosIds() != null) {
                        for (Long donacionSegId : parada.getEnviosIds()) {

                            Long entidadId = loteDonaciones.stream()
                                    .filter(dto -> dto.donacionSegmentadaId().equals(donacionSegId))
                                    .map(DonacionSegmentadaListaParaEntregarALogisticaDTO::entidadBeneficiariaId)
                                    .findFirst()
                                    .orElse(null);

                            Envio nuevoEnvio = new Envio();
                            nuevoEnvio.setDonacionSegmentadaId(donacionSegId);
                            nuevoEnvio.setEntidadBeneficiariaId(entidadId);

                            Envio envioGuardado = enviosService.registrarEnvio(nuevoEnvio);

                            enviosRealesIds.add(envioGuardado.getId());
                        }
                    }
                    parada.setEnviosIds(enviosRealesIds);
                }
            }
        }

        List<Ruta> rutasGuardadas = (List<Ruta>) rutaRepository.saveAll(nuevasRutas);

        for (Ruta rutaGuardada : rutasGuardadas) {
            this.registrarRuta(rutaGuardada);
        }

        logger.info("Planificación finalizada. Se generaron exitosamente {} rutas.", rutasGuardadas.size());
    }
    public Ruta registrarRuta(Ruta ruta) {
        ruta.setIniciada(false);

        if (ruta.getParadas() != null) {
            for (Parada parada : ruta.getParadas()) {
                if (parada.getEnviosIds() != null) {
                    for (Long envioId : parada.getEnviosIds()) {
                        Envio envio = enviosService.buscarEnvioPorId(envioId);
                        if (envio != null) {
                            envio.setEstado(EstadoEnvio.ASIGNACION_REALIZADA);
                            envio.setRutaId(ruta.getId());
                        }
                    }
                }
            }
        }

        return rutaRepository.save(ruta);
    }

    public void iniciarRuta(Long rutaId) {
        Ruta ruta = rutaRepository.findById(rutaId);
        if (ruta == null) {
            throw new IllegalArgumentException("No se encontró la ruta con ID: " + rutaId);
        }

        ruta.iniciarRuta();

        String patente = "DESCONOCIDO";
        if (ruta.getCamion() != null && ruta.getCamion().getId() != null) {
            Camion camion = camionService.buscarCamionPorId(ruta.getCamion().getId());
            if (camion != null) {
                patente = camion.getPatente();
            }
        }

        if (ruta.getParadas() != null) {
            for (Parada parada : ruta.getParadas()) {
                if (parada.getEnviosIds() != null) {
                    for (Long envioId : parada.getEnviosIds()) {
                        Envio envio = enviosService.buscarEnvioPorId(envioId);
                        if (envio != null) {
                            envio.setEstado(EstadoEnvio.EN_TRASLADO);

                            EventoLogistica evento = EventoLogistica.builder()
                                    .tipoEvento("INICIO_RUTA")
                                    .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                                    .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                                    .timestamp(LocalDateTime.now())
                                    .detalles("Patente del camión: " + patente + ", Chofer ID: " + ruta.getChofer().getId())
                                    .build();

                            eventRepository.registrar(evento);
                        }
                    }
                }
            }
        }

        rutaRepository.save(ruta);
    }

    public List<Ruta> listarRutas() {
        return rutaRepository.findAll();
    }

    public Ruta buscarRutaPorId(Long id) {
        if (id == null) return null;
        return rutaRepository.findById(id);
    }
}