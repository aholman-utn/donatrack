package com.tp.donatrack.logistica.services;

import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.EstadoEnvio;
import com.tp.donatrack.logistica.domain.EventoLogistica;
import com.tp.donatrack.logistica.domain.Parada;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.domain.planificacion.Planificacion;
import com.tp.donatrack.logistica.repository.LogisticaEventRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.tp.donatrack.logistica.repository.RutaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogisticaService {
    private static final Logger logger = LoggerFactory.getLogger(LogisticaService.class);

    private final LogisticaEventRepository eventRepository;
    private final RutaRepository rutaRepository;
    private final Map<Long, Camion> camiones = new ConcurrentHashMap<>();
    private final Map<Long, Chofer> choferes = new ConcurrentHashMap<>();
    private final Map<Long, Envio> envios = new ConcurrentHashMap<>();

    private final AtomicLong camionIdSeq = new AtomicLong(1);
    private final AtomicLong choferIdSeq = new AtomicLong(1);
    private final AtomicLong envioIdSeq = new AtomicLong(1);
    private final Planificacion planificacion;

    public LogisticaService(
        LogisticaEventRepository eventRepository,
        RutaRepository rutaRepository,
        Planificacion planificacion
    ) {
        this.eventRepository = eventRepository;
        this.rutaRepository = rutaRepository;
        this.planificacion = planificacion;
    }

    public Camion registrarCamion(Camion camion) {
        Long id = camionIdSeq.getAndIncrement();
        camion.setId(id);
        camiones.put(id, camion);
        return camion;
    }

    public void planificarLote(List<DonacionSegmentadaListaParaEntregarALogisticaDTO> loteDonaciones) {
        if (loteDonaciones == null || loteDonaciones.isEmpty()) {
            logger.warn("Se recibió un lote de planificación vacío.");
            return;
        }

        List<Camion> camionesDisponibles = new ArrayList<>(camiones.values());
        List<Chofer> choferesDisponibles = new ArrayList<>(choferes.values());

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

        rutaRepository.saveAll(nuevasRutas);

        logger.info("Planificación finalizada. Se generaron exitosamente {} rutas.", nuevasRutas.size());
    }

    public Chofer registrarChofer(Chofer chofer) {
        Long id = choferIdSeq.getAndIncrement();
        chofer.setId(id);
        choferes.put(id, chofer);
        return chofer;
    }

    public Envio registrarEnvio(Envio envio) {
        Long id = envioIdSeq.getAndIncrement();
        envio.setId(id);
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envios.put(id, envio);
        return envio;
    }

    public Ruta registrarRuta(Ruta ruta) {
        ruta.setIniciada(false);

        if (ruta.getParadas() != null) {
            for (Parada parada : ruta.getParadas()) {
                if (parada.getEnviosIds() != null) {
                    for (Long envioId : parada.getEnviosIds()) {
                        Envio envio = envios.get(envioId);
                        if (envio != null) {
                            envio.setEstado(EstadoEnvio.ASIGNACION_REALIZADA);
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
            Camion camion = camiones.get(ruta.getCamion().getId());
            if (camion != null) {
                patente = camion.getPatente();
            }
        }

        if (ruta.getParadas() != null) {
            for (Parada parada : ruta.getParadas()) {
                if (parada.getEnviosIds() != null) {
                    for (Long envioId : parada.getEnviosIds()) {
                        Envio envio = envios.get(envioId);
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

    public void registrarEntregaExitosa(Long envioId, String detalles) {
        Envio envio = envios.get(envioId);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró el envío con ID: " + envioId);
        }

        envio.registrarRecepcionExitosa(); // transitions state and validates

        EventoLogistica evento = EventoLogistica.builder()
                .tipoEvento("ENTREGA_EXITOSA")
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(LocalDateTime.now())
                .detalles(detalles != null ? detalles : "Entrega realizada correctamente")
                .build();

        eventRepository.registrar(evento);
    }

    public void registrarEntregaFallida(Long envioId, String motivo) {
        Envio envio = envios.get(envioId);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró el envío con ID: " + envioId);
        }

        envio.registrarRecepcionFallida(); // transitions state and validates

        EventoLogistica evento = EventoLogistica.builder()
                .tipoEvento("ENTREGA_FALLIDA")
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(LocalDateTime.now())
                .detalles(motivo != null ? motivo : "Recepción rechazada / Chofer no pudo entregar")
                .build();

        eventRepository.registrar(evento);
    }

    public List<Camion> listarCamiones() {
        return new ArrayList<>(camiones.values());
    }

    public List<Chofer> listarChoferes() {
        return new ArrayList<>(choferes.values());
    }

    public List<Envio> listarEnvios() {
        return new ArrayList<>(envios.values());
    }

    public List<Ruta> listarRutas() {
        return rutaRepository.findAll();
    }
}
