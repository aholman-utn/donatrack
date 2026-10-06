package com.tp.donatrack.logistica.services;

import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.EstadoEnvio;
import com.tp.donatrack.logistica.domain.EventoLogistica;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.repository.EnvioRepository;
import com.tp.donatrack.logistica.repository.LogisticaEventRepository;
import com.tp.donatrack.logistica.clients.DonacionesQueueClient;
import com.tp.commons.dtos.logistica.EventoLogisticaDTO;
import com.tp.commons.dtos.logistica.TipoEventoLogistica;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EnviosService {
    private final LogisticaEventRepository eventRepository;
    private final EnvioRepository envioRepository;
    private final RutaService rutaService;
    private final CamionService camionService;
    private final DonacionesQueueClient donacionesQueueClient;

    public EnviosService(
            LogisticaEventRepository eventRepository,
            EnvioRepository envioRepository,
            RutaService rutaService,
            CamionService camionService,
            DonacionesQueueClient donacionesQueueClient
    ) {
        this.eventRepository = eventRepository;
        this.envioRepository = envioRepository;
        this.rutaService = rutaService;
        this.camionService = camionService;
        this.donacionesQueueClient = donacionesQueueClient;
    }

    public Envio registrarEnvio(Envio envio) {
        if (envio.getEstado() == null) {
            envio.setEstado(EstadoEnvio.PENDIENTE);
        }
        return envioRepository.save(envio);
    }

    public void registrarLlegadaADestino(Long envioId) {
        Envio envio = buscarEnvioPorId(envioId);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró el envío con ID: " + envioId);
        }

        envio.registrarEnDestino();
        envioRepository.save(envio);

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

        EventoLogisticaDTO dto = EventoLogisticaDTO.builder()
                .tipoEvento(TipoEventoLogistica.LLEGADA_A_DESTINO)
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(evento.getTimestamp())
                .detalles(evento.getDetalles())
                .build();
        donacionesQueueClient.publicarEvento(dto);
    }

    public void registrarEntregaExitosa(Long envioId, String detallesExtra) {
        Envio envio = buscarEnvioPorId(envioId);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró el envío con ID: " + envioId);
        }

        envio.registrarRecepcionExitosa();
        envioRepository.save(envio);

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

        EventoLogisticaDTO dto = EventoLogisticaDTO.builder()
                .tipoEvento(TipoEventoLogistica.ENTREGA_EXITOSA)
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(evento.getTimestamp())
                .detalles(evento.getDetalles())
                .build();
        donacionesQueueClient.publicarEvento(dto);
    }

    public void registrarEntregaFallida(Long envioId, String motivo) {
        Envio envio = buscarEnvioPorId(envioId);
        if (envio == null) {
            throw new IllegalArgumentException("No se encontró el envío con ID: " + envioId);
        }

        envio.registrarRecepcionFallida();
        envioRepository.save(envio);

        EventoLogistica evento = EventoLogistica.builder()
                .tipoEvento("ENTREGA_FALLIDA")
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(LocalDateTime.now())
                .detalles(motivo != null ? motivo : "Recepción rechazada / Chofer no pudo entregar")
                .build();

        eventRepository.registrar(evento);

        EventoLogisticaDTO dto = EventoLogisticaDTO.builder()
                .tipoEvento(TipoEventoLogistica.ENTREGA_FALLIDA)
                .donacionSegmentadaId(envio.getDonacionSegmentadaId())
                .entidadBeneficiariaId(envio.getEntidadBeneficiariaId())
                .timestamp(evento.getTimestamp())
                .detalles(evento.getDetalles())
                .build();
        donacionesQueueClient.publicarEvento(dto);
    }

    @Transactional(readOnly = true)
    public List<Envio> listarEnvios() {
        return envioRepository.findAll();
    }

    private Camion buscarCamionPorEnvio(Long envioId) {
        Envio envio = buscarEnvioPorId(envioId);

        if (envio != null && envio.getRutaId() != null) {
            Ruta ruta = rutaService.buscarRutaPorId(envio.getRutaId());

            if (ruta != null && ruta.getCamion() != null) {
                return camionService.buscarCamionPorId(ruta.getCamion().getId());
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Envio buscarEnvioPorId(Long id) {
        if (id == null) return null;
        return envioRepository.findById(id).orElse(null);
    }
}