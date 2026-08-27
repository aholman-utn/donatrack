package com.tp.donatrack.logistica.services;

import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.EstadoEnvio;
import com.tp.donatrack.logistica.domain.EventoLogistica;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.repository.LogisticaEventRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class EnviosService {
    private final LogisticaEventRepository eventRepository;

    private final RutaService rutaService;
    private final CamionService camionService;

    private final Map<Long, Envio> envios = new ConcurrentHashMap<>();
    private final AtomicLong envioIdSeq = new AtomicLong(1);

    public EnviosService(
            LogisticaEventRepository eventRepository,
            RutaService rutaService,
            CamionService camionService
    ) {
        this.eventRepository = eventRepository;
        this.rutaService = rutaService;
        this.camionService = camionService;
    }

    public Envio registrarEnvio(Envio envio) {
        Long id = envioIdSeq.getAndIncrement();
        envio.setId(id);
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envios.put(id, envio);
        return envio;
    }

    public void registrarLlegadaADestino(Long envioId) {
        Envio envio = envios.get(envioId);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró el envío con ID: " + envioId);
        }

        envio.registrarEnDestino();

        Camion camionResponsable = buscarCamionPorEnvio(envioId);
        String infoCamion = (camionResponsable != null)
                ? "Patente: " + camionResponsable.getPatente()
                : "Vehículo no identificado";

        EventoLogistica evento = EventoLogistica.builder()
                .tipoEvento("LLEGADA_A_DESTINO")
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(LocalDateTime.now())
                .detalles("El camión llegó a la entidad. " + infoCamion)
                .build();

        eventRepository.registrar(evento);
    }

    public void registrarEntregaExitosa(Long envioId, String detallesExtra) {
        Envio envio = envios.get(envioId);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró el envío con ID: " + envioId);
        }

        envio.registrarRecepcionExitosa();

        Camion camionResponsable = buscarCamionPorEnvio(envioId);
        String infoCamion = (camionResponsable != null)
                ? "Patente: " + camionResponsable.getPatente()
                : "Vehículo no identificado";

        String detallesFinales = (detallesExtra != null ? detallesExtra + " - " : "Entrega realizada correctamente. ") + infoCamion;

        EventoLogistica evento = EventoLogistica.builder()
                .tipoEvento("ENTREGA_EXITOSA")
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(LocalDateTime.now())
                .detalles(detallesFinales)
                .build();

        eventRepository.registrar(evento);
    }

    public void registrarEntregaFallida(Long envioId, String motivo) {
        Envio envio = envios.get(envioId);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró el envío con ID: " + envioId);
        }

        envio.registrarRecepcionFallida();

        EventoLogistica evento = EventoLogistica.builder()
                .tipoEvento("ENTREGA_FALLIDA")
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(LocalDateTime.now())
                .detalles(motivo != null ? motivo : "Recepción rechazada / Chofer no pudo entregar")
                .build();

        eventRepository.registrar(evento);
    }

    public List<Envio> listarEnvios() {
        return new ArrayList<>(envios.values());
    }

    private Camion buscarCamionPorEnvio(Long envioId) {
        Envio envio = envios.get(envioId);

        if (envio != null && envio.getRutaId() != null) {
            Ruta ruta = rutaService.buscarRutaPorId(envio.getRutaId());

            if (ruta != null && ruta.getCamion() != null) {
                return camionService.buscarCamionPorId(ruta.getCamion().getId());
            }
        }
        return null;
    }

    public Envio buscarEnvioPorId(Long id) {
        return envios.get(id);
    }
}