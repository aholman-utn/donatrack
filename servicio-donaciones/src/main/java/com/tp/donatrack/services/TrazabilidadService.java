package com.tp.donatrack.services;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.services.notificador.NotificacionRestClient;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;

import com.tp.donatrack.dtos.CrearEventoRequest;
import com.tp.donatrack.dtos.TrazaDonacionDTO;
import com.tp.donatrack.dtos.TrazaSegmentoDTO;

import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.DonanteRepository;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de la trazabilidad de donaciones segmentadas
 * y del envío de notificaciones ante eventos de logística.
 */
@Service
public class TrazabilidadService {

    private static final Logger logger = LoggerFactory.getLogger(TrazabilidadService.class);

    private final DonacionRepository donacionRepository;
    private final DonanteRepository donanteRepository;
    private final EntidadBeneficiariaRepository entidadBeneficiariaRepository;
    private final NotificacionRestClient notificacionRestClient;

    public TrazabilidadService(
            DonacionRepository donacionRepository,
            DonanteRepository donanteRepository,
            EntidadBeneficiariaRepository entidadBeneficiariaRepository,
            NotificacionRestClient notificacionRestClient) {
        this.donacionRepository = donacionRepository;
        this.donanteRepository = donanteRepository;
        this.entidadBeneficiariaRepository = entidadBeneficiariaRepository;
        this.notificacionRestClient = notificacionRestClient;
    }

    private Donacion buscarDonacionPorId(Integer id) {
        Donacion donacion = donacionRepository.findById(id);
        if (donacion == null) {
            throw new IllegalArgumentException("No se encontró la donación con ID: " + id);
        }
        return donacion;
    }

    private DonacionSegmentada buscarDonacionSegmentadaPorId(Integer idDonacion, Integer idSegmentada) {
        Donacion donacion = this.buscarDonacionPorId(idDonacion);
        return donacion.getDonacionesSegmentadas().stream()
                .filter(p -> p.getId() != null && p.getId().equals(Long.valueOf(idSegmentada)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la el segmento de la donación con ID: " + idSegmentada));
    }

    public TrazaDonacionDTO trazabilizarDonacion(Integer id) {
        Donacion donacion = this.buscarDonacionPorId(id);

        List<DonacionSegmentada> segmentos = donacion.getDonacionesSegmentadas();
        List<TrazaSegmentoDTO> trazabilidades = new ArrayList<>();
        for (DonacionSegmentada segmentada : segmentos) {
            trazabilidades.add(
                    TrazaSegmentoDTO.builder()
                            .id(Math.toIntExact(segmentada.getId()))
                            .eventos(segmentada.getHistorial())
                            .build());
        }
        return TrazaDonacionDTO.builder()
                .id(donacion.getId())
                .estado(donacion.getEstado())
                .segmentos(trazabilidades)
                .build();
    }

    public TrazaSegmentoDTO trazabilizarDonacionSegmentada(Integer idDonacion, Integer idSegmento) {
        DonacionSegmentada segmentada = buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionarDonacion(
            Integer idDonacion,
            Integer idSegmento,
            CrearEventoRequest request) {
        DonacionSegmentada segmentada = buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), request.getNuevoEstado())) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }

        segmentada.transicionar(
                request.getNuevoEstado(),
                request.getActor(),
                request.getDescripcion());

        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionListaEntregar(
            Integer idDonacion,
            Integer idSegmento,
            String actor) {
        DonacionSegmentada segmentada = buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR)) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }
        segmentada.listarParaEntrega(actor);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionEnTraslado(
            Integer idDonacion,
            Integer idSegmento,
            String actor) {
        DonacionSegmentada segmentada = buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.EN_TRASLADO)) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }
        segmentada.iniciarTraslado(actor);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionEntregaFallida(
            Integer idDonacion,
            Integer idSegmento,
            String actor,
            String justificacion) {
        DonacionSegmentada segmentada = buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.ENTREGA_FALLIDA)) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }
        segmentada.registrarEntregaFallida(actor, justificacion);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionMarcarVencida(
            Integer idDonacion,
            Integer idSegmento,
            String actor) {
        DonacionSegmentada segmentada = buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.VENCIDA)) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }
        segmentada.marcarVencida(actor);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    /**
     * Permite a la entidad beneficiaria confirmar la recepción de una donación.
     * Transiciona el estado a ENTREGADA y envía notificación al donante y a la entidad.
     *
     * @param idDonacion  ID de la donación padre
     * @param idSegmento  ID de la donación segmentada a recepcionar
     * @return traza actualizada del segmento
     */
    public TrazaSegmentoDTO recepcionarEntrega(Integer idDonacion, Integer idSegmento) {
        DonacionSegmentada segmentada = buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.ENTREGADA)) {
            throw new IllegalArgumentException("No es posible recepcionar esta donación en su estado actual: " + segmentada.getEstado());
        }
        segmentada.confirmarEntrega(segmentada.getEntidadBeneficiariaAsignadaId());

        Donacion donacion = buscarDonacionPorDonacionSegmentada(segmentada);
        if (donacion != null) {
            notificarEntregaExitosa(donacion, segmentada);
        }

        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    /**
     * Notifica al donante y a la entidad beneficiaria que la donación inició la ruta de entrega.
     * Invocado desde el polling de logística al detectar un evento INICIO_RUTA.
     *
     * @param segmentada la donación segmentada que pasó a EN_TRASLADO
     */
    public void notificarInicioDeRuta(DonacionSegmentada segmentada) {
        Donacion donacion = buscarDonacionPorDonacionSegmentada(segmentada);
        if (donacion == null) return;

        String categoria = segmentada.getSubCategoria() != null
                ? segmentada.getSubCategoria().getDescripcion()
                : "donación";

        if (donacion.getDonante() != null) {
            Donante donante = donacion.getDonante();
            String contacto = donante.getPersona().getContactoPredeterminado();
            TipoNotificador medio = donante.getPersona().getTipoNotificadorPreferido();

            if (contacto != null) {
                String asunto = "Tu donación está en camino";
                String mensaje = String.format(
                        "Hola %s, tu donación de %s ya inició la ruta de entrega hacia la entidad beneficiaria. ¡Gracias por tu generosidad!",
                        donante.getNombreCompleto(), categoria);
                enviarNotificacionSegura(medio, contacto, mensaje, asunto, donante.getPersona().getId());
            }
        }

        if (segmentada.getEntidadBeneficiariaAsignadaId() != null) {
            EntidadBeneficiaria entidad = entidadBeneficiariaRepository.find(segmentada.getEntidadBeneficiariaAsignadaId());
            if (entidad != null && entidad.getDatosDeEntidad() != null) {
                String contactoEntidad = entidad.getDatosDeEntidad().getContactoPredeterminado();
                TipoNotificador medioEntidad = entidad.getDatosDeEntidad().getTipoNotificadorPreferido();

                if (contactoEntidad != null) {
                    String asunto = "Una donación está en camino hacia tu entidad";
                    String mensaje = String.format(
                            "La donación de %s ya se encuentra en traslado hacia tu ubicación. Preparate para recibirla.",
                            categoria);
                    enviarNotificacionSegura(medioEntidad, contactoEntidad, mensaje, asunto, entidad.getDatosDeEntidad().getId());
                }
            }
        }
    }

    /**
     * Notifica al donante y a la entidad beneficiaria que la entrega no pudo completarse.
     * Invocado desde el polling de logística al detectar un evento ENTREGA_FALLIDA.
     *
     * @param segmentada   la donación segmentada con entrega fallida
     * @param justificacion motivo por el cual no se pudo entregar
     */
    public void notificarEntregaNoSatisfactoria(DonacionSegmentada segmentada, String justificacion) {
        Donacion donacion = buscarDonacionPorDonacionSegmentada(segmentada);
        if (donacion == null) return;

        String categoria = segmentada.getSubCategoria() != null
                ? segmentada.getSubCategoria().getDescripcion()
                : "donación";

        if (donacion.getDonante() != null) {
            Donante donante = donacion.getDonante();
            String contacto = donante.getPersona().getContactoPredeterminado();
            TipoNotificador medio = donante.getPersona().getTipoNotificadorPreferido();

            if (contacto != null) {
                String asunto = "Hubo un problema con la entrega de tu donación";
                String mensaje = String.format(
                        "Hola %s, lamentamos informarte que la entrega de tu donación de %s no pudo completarse. Motivo: %s. La donación fue devuelta al depósito y se intentará nuevamente.",
                        donante.getNombreCompleto(), categoria, justificacion);
                enviarNotificacionSegura(medio, contacto, mensaje, asunto, donante.getPersona().getId());
            }
        }

        if (segmentada.getEntidadBeneficiariaAsignadaId() != null) {
            EntidadBeneficiaria entidad = entidadBeneficiariaRepository.find(segmentada.getEntidadBeneficiariaAsignadaId());
            if (entidad != null && entidad.getDatosDeEntidad() != null) {
                String contactoEntidad = entidad.getDatosDeEntidad().getContactoPredeterminado();
                TipoNotificador medioEntidad = entidad.getDatosDeEntidad().getTipoNotificadorPreferido();

                if (contactoEntidad != null) {
                    String asunto = "La entrega no pudo completarse";
                    String mensaje = String.format(
                            "La donación de %s no pudo ser entregada. Motivo: %s. Se intentará realizar una nueva entrega próximamente.",
                            categoria, justificacion);
                    enviarNotificacionSegura(medioEntidad, contactoEntidad, mensaje, asunto, entidad.getDatosDeEntidad().getId());
                }
            }
        }
    }

    /**
     * Notifica al donante y a la entidad beneficiaria que la entrega fue exitosa.
     * Invocado desde recepcionarEntrega cuando la entidad confirma la recepción.
     */
    private void notificarEntregaExitosa(Donacion donacion, DonacionSegmentada segmentada) {
        String categoria = segmentada.getSubCategoria() != null
                ? segmentada.getSubCategoria().getDescripcion()
                : "donación";

        if (donacion.getDonante() != null) {
            Donante donante = donacion.getDonante();
            String contacto = donante.getPersona().getContactoPredeterminado();
            TipoNotificador medio = donante.getPersona().getTipoNotificadorPreferido();

            if (contacto != null) {
                String asunto = "¡Tu donación fue entregada con éxito!";
                String mensaje = String.format(
                        "Hola %s, tu donación de %s fue entregada exitosamente a la entidad beneficiaria. ¡Gracias por hacer la diferencia!",
                        donante.getNombreCompleto(), categoria);
                enviarNotificacionSegura(medio, contacto, mensaje, asunto, donante.getPersona().getId());
            }
        }

        if (segmentada.getEntidadBeneficiariaAsignadaId() != null) {
            EntidadBeneficiaria entidad = entidadBeneficiariaRepository.find(segmentada.getEntidadBeneficiariaAsignadaId());
            if (entidad != null && entidad.getDatosDeEntidad() != null) {
                String contactoEntidad = entidad.getDatosDeEntidad().getContactoPredeterminado();
                TipoNotificador medioEntidad = entidad.getDatosDeEntidad().getTipoNotificadorPreferido();

                if (contactoEntidad != null) {
                    String asunto = "Entrega recibida exitosamente";
                    String mensaje = String.format(
                            "La donación de %s fue entregada y confirmada en tu entidad. ¡Gracias por ser parte de la red DonaTrack!",
                            categoria);
                    enviarNotificacionSegura(medioEntidad, contactoEntidad, mensaje, asunto, entidad.getDatosDeEntidad().getId());
                }
            }
        }
    }

    /**
     * Busca la donación padre que contiene una donación segmentada dada.
     */
    private Donacion buscarDonacionPorDonacionSegmentada(DonacionSegmentada segmentada) {
        return donacionRepository.findAll().stream()
                .filter(d -> d.getDonacionesSegmentadas().contains(segmentada))
                .findFirst()
                .orElse(null);
    }

    /**
     * Envía una notificación de forma segura (fire-and-forget).
     * Si falla el envío, loguea el error pero no interrumpe la operación en curso.
     */
    private void enviarNotificacionSegura(TipoNotificador medio, String contacto, String mensaje, String asunto, Long personaId) {
        try {
            notificacionRestClient.notificar(medio, contacto, mensaje, asunto, personaId);
        } catch (Exception e) {
            logger.error("Error al enviar notificación a persona {}: {}", personaId, e.getMessage());
        }
    }

}
