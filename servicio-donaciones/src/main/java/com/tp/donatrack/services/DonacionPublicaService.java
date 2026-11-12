package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;
import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.dtos.DonacionPublicaDTO;
import com.tp.donatrack.repositories.DonacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Consultas públicas (sin autenticación) sobre donaciones entregadas:
 * las destacadas del último mes para la landing y las entregas para el mapa.
 */
@Service
public class DonacionPublicaService {

    private static final String ENTREGADA = "ENTREGADA";

    private final DonacionRepository donacionRepository;
    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public DonacionPublicaService(DonacionRepository donacionRepository,
                                  EntidadBeneficiariaService entidadBeneficiariaService) {
        this.donacionRepository = donacionRepository;
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    /** Entregas de los últimos 30 días, de la más reciente a la más vieja. */
    @Transactional(readOnly = true)
    public List<DonacionPublicaDTO> destacadasUltimoMes(int limite) {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        return entregadas().stream()
                .filter(d -> d.fechaEntrega() != null && d.fechaEntrega().isAfter(desde))
                .limit(Math.max(1, Math.min(limite, 50)))
                .toList();
    }

    /** Entregas que tienen coordenadas, para dibujarlas en el mapa. */
    @Transactional(readOnly = true)
    public List<DonacionPublicaDTO> entregasParaMapa() {
        return entregadas().stream()
                .filter(d -> d.latitud() != null && d.longitud() != null)
                .toList();
    }

    private List<DonacionPublicaDTO> entregadas() {
        List<DonacionSegmentada> segmentadas = donacionRepository.findAll().stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(s -> s.getEstado() != null && ENTREGADA.equals(s.getEstado().getNombre()))
                .toList();

        Map<Long, EntidadBeneficiaria> entidades = entidadesPorId(segmentadas.stream()
                .map(DonacionSegmentada::getEntidadBeneficiariaAsignadaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        return segmentadas.stream()
                .map(s -> aDTO(s, entidades.get(s.getEntidadBeneficiariaAsignadaId())))
                .sorted(Comparator.comparing(DonacionPublicaDTO::fechaEntrega,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /**
     * La segmentada guarda el id de la persona jurídica de la entidad
     * (EnDeposito.asignar), así que se busca y se indexa por ese id.
     */
    private Map<Long, EntidadBeneficiaria> entidadesPorId(Collection<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return entidadBeneficiariaService.listarPorPersonaJuridicaIds(ids).stream()
                .filter(e -> e.getDatosDeEntidad() != null)
                .collect(Collectors.toMap(e -> e.getDatosDeEntidad().getId(), Function.identity(),
                        (a, b) -> a));
    }

    private DonacionPublicaDTO aDTO(DonacionSegmentada s, EntidadBeneficiaria entidad) {
        PersonaJuridica pj = entidad != null ? entidad.getDatosDeEntidad() : null;
        Direccion dir = pj != null ? pj.getDireccion() : null;

        String nombreEntidad = s.getEntidadBeneficiariaNombre() != null
                ? s.getEntidadBeneficiariaNombre()
                : (pj != null ? pj.getRazonSocial() : null);

        var sub = s.getSubCategoria();
        String categoria = s.getCategoriaBien() != null ? s.getCategoriaBien().name()
                : (sub != null && sub.getCategoria() != null ? sub.getCategoria().name() : null);

        return new DonacionPublicaDTO(
                s.getId(),
                categoria,
                sub != null ? sub.getDescripcion() : null,
                s.getCantidad(),
                sub != null && sub.getUnidad() != null ? sub.getUnidad().name() : null,
                nombreEntidad,
                dir != null ? dir.getDireccion() : null,
                dir != null ? dir.getLatitud() : null,
                dir != null ? dir.getLongitud() : null,
                fechaEntrega(s));
    }

    /** Fecha del evento que pasó la donación a ENTREGADA; si no hay, la del comprobante. */
    private LocalDateTime fechaEntrega(DonacionSegmentada s) {
        LocalDateTime porHistorial = s.getHistorial().stream()
                .filter(e -> e.getEstadoNuevo() != null && ENTREGADA.equals(e.getEstadoNuevo().getNombre()))
                .map(EventoTrazabilidad::getFecha)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        if (porHistorial != null) return porHistorial;
        var comprobante = s.getComprobanteRecepcionDonacion();
        return comprobante != null ? comprobante.getFechaEntrega() : null;
    }
}
