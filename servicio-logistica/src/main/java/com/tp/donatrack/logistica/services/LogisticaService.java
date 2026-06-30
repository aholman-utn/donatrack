package com.tp.donatrack.logistica.services;

import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.EstadoEnvio;
import com.tp.donatrack.logistica.domain.EventoLogistica;
import com.tp.donatrack.logistica.domain.Parada;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.repository.LogisticaEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LogisticaService {

    private final LogisticaEventRepository eventRepository;

    private final Map<Long, Camion> camiones = new ConcurrentHashMap<>();
    private final Map<Long, Chofer> choferes = new ConcurrentHashMap<>();
    private final Map<Long, Envio> envios = new ConcurrentHashMap<>();
    private final Map<Long, Ruta> rutas = new ConcurrentHashMap<>();

    private final AtomicLong camionIdSeq = new AtomicLong(1);
    private final AtomicLong choferIdSeq = new AtomicLong(1);
    private final AtomicLong envioIdSeq = new AtomicLong(1);
    private final AtomicLong rutaIdSeq = new AtomicLong(1);

    public LogisticaService(LogisticaEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Camion registrarCamion(Camion camion) {
        Long id = camionIdSeq.getAndIncrement();
        camion.setId(id);
        camiones.put(id, camion);
        return camion;
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
        Long id = rutaIdSeq.getAndIncrement();
        ruta.setId(id);
        ruta.setIniciada(false);
        rutas.put(id, ruta);

        // Al asignar envíos a la ruta, marcamos su estado como ASIGNACION_REALIZADA
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
        return ruta;
    }

    public void iniciarRuta(Long rutaId) {
        Ruta ruta = rutas.get(rutaId);
        if (ruta == null) {
            throw new IllegalArgumentException("No se encontró la ruta con ID: " + rutaId);
        }

        ruta.iniciarRuta(); // triggers state change inside domain and checks if already started

        Camion camion = camiones.get(ruta.getCamionId());
        String patente = (camion != null) ? camion.getPatente() : "DESCONOCIDO";

        // De acuerdo a las transiciones exigidas, marcamos los envíos de la ruta EN_TRASLADO y generamos eventos
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
                                    .detalles("Patente del camión: " + patente + ", Chofer ID: " + ruta.getChoferId())
                                    .build();

                            eventRepository.registrar(evento);
                        }
                    }
                }
            }
        }
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
        return new ArrayList<>(rutas.values());
    }
}
