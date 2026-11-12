package com.tp.donatrack.controllers;

import com.tp.donatrack.clients.NotificacionesRestClient;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.donacion.FotoRecepcion;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import com.tp.donatrack.repositories.FotoRecepcionRepository;
import com.tp.donatrack.services.TrazabilidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Portal de la entidad beneficiaria: donaciones asignadas, confirmación de
 * recepción con fotos y aviso de entrega no recibida.
 * Todas las operaciones se limitan a la entidad del JWT (claim entidad_beneficiaria_id).
 */
@RestController
@RequestMapping("/entidades/me")
public class PortalEntidadController {

    private static final int MAX_FOTOS = 4;
    private static final Set<String> TIPOS_FOTO = Set.of("image/jpeg", "image/png", "image/webp");

    private final EntidadBeneficiariaRepository entidades;
    private final DonacionRepository donaciones;
    private final FotoRecepcionRepository fotos;
    private final TrazabilidadService trazabilidad;
    private final NotificacionesRestClient notificaciones;

    public PortalEntidadController(EntidadBeneficiariaRepository entidades, DonacionRepository donaciones,
            FotoRecepcionRepository fotos, TrazabilidadService trazabilidad, NotificacionesRestClient notificaciones) {
        this.notificaciones = notificaciones;
        this.entidades = entidades;
        this.donaciones = donaciones;
        this.fotos = fotos;
        this.trazabilidad = trazabilidad;
    }

    @GetMapping("/donaciones")
    @Transactional(readOnly = true)
    public List<DonacionRecibida> donaciones(@AuthenticationPrincipal Jwt jwt) {
        EntidadBeneficiaria entidad = titular(jwt);
        Long personaId = entidad.getDatosDeEntidad().getId();

        List<DonacionSegmentada> propios = new ArrayList<>();
        List<DonacionRecibida> resultado = new ArrayList<>();
        List<Donacion> lista = donaciones.findConSegmentosAsignadosA(personaId);
        lista.forEach(d -> d.getDonacionesSegmentadas().stream()
                .filter(s -> personaId.equals(s.getEntidadBeneficiariaAsignadaId())).forEach(propios::add));

        Map<Long, List<Long>> fotosPorSegmento = propios.isEmpty() ? Map.of()
                : fotos.findIdsPorSegmento(propios.stream().map(DonacionSegmentada::getId).toList()).stream()
                        .collect(Collectors.groupingBy(f -> (Long) f[0],
                                Collectors.mapping(f -> (Long) f[1], Collectors.toList())));

        for (Donacion d : lista) {
            List<Segmento> segmentos = d.getDonacionesSegmentadas().stream()
                    .filter(s -> personaId.equals(s.getEntidadBeneficiariaAsignadaId()))
                    .map(s -> segmento(s, fotosPorSegmento.getOrDefault(s.getId(), List.of())))
                    .toList();
            resultado.add(new DonacionRecibida(d.getId(), d.getDescripcion(), d.getFechaIngreso(),
                    d.getDonante() != null ? d.getDonante().getNombreCompleto() : null, segmentos));
        }
        resultado.sort(Comparator.comparing(DonacionRecibida::fechaIngreso,
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
        return resultado;
    }

    /**
     * Confirma la recepción de una donación en traslado. Acepta hasta 4 fotos
     * (JPG, PNG o WEBP de hasta 2 MB cada una) en el campo multipart "fotos".
     */
    @PostMapping(value = "/donaciones/{idDonacion}/segmentos/{idSegmento}/recepcion",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public Object confirmarRecepcion(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idDonacion,
            @PathVariable Long idSegmento, @RequestParam(value = "fotos", required = false) List<MultipartFile> archivos) {
        EntidadBeneficiaria entidad = titular(jwt);
        DonacionSegmentada segmento = segmentoPropio(entidad, idDonacion, idSegmento);
        if (!EstadoDonacionSegmentada.EN_TRASLADO.equals(segmento.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede confirmar una donación que está en camino.");
        }
        List<MultipartFile> validas = archivos == null ? List.of()
                : archivos.stream().filter(f -> f != null && !f.isEmpty()).toList();
        if (validas.size() > MAX_FOTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Podés cargar hasta " + MAX_FOTOS + " fotos.");
        }
        List<FotoRecepcion> nuevas = new ArrayList<>();
        for (MultipartFile f : validas) {
            if (f.getContentType() == null || !TIPOS_FOTO.contains(f.getContentType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fotos deben ser JPG, PNG o WEBP.");
            }
            if (f.getSize() > FotoRecepcion.TAMANIO_MAXIMO) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada foto puede pesar hasta 2 MB.");
            }
            try {
                nuevas.add(new FotoRecepcion(idSegmento, entidad.getIdEntidadBeneficiaria(), f.getContentType(), f.getBytes()));
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer una de las fotos.");
            }
        }
        String detalle = "Recepción confirmada por " + entidad.getDatosDeEntidad().getRazonSocial()
                + (nuevas.isEmpty() ? "" : " con " + nuevas.size() + (nuevas.size() == 1 ? " foto" : " fotos"));
        var traza = trazabilidad.recepcionarEntrega(idDonacion, idSegmento, LocalDateTime.now(), detalle);
        fotos.saveAll(nuevas);
        return traza;
    }

    public record NoRecibidaRequest(String motivo) {}

    /** La entidad informa que no recibió una donación en traslado; queda para revisión de administración. */
    @PostMapping("/donaciones/{idDonacion}/segmentos/{idSegmento}/no-recibida")
    @Transactional
    public Object informarNoRecibida(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idDonacion,
            @PathVariable Long idSegmento, @RequestBody NoRecibidaRequest request) {
        EntidadBeneficiaria entidad = titular(jwt);
        DonacionSegmentada segmento = segmentoPropio(entidad, idDonacion, idSegmento);
        String motivo = request == null || request.motivo() == null ? "" : request.motivo().trim();
        if (motivo.isEmpty() || motivo.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contanos brevemente qué pasó (hasta 500 caracteres).");
        }
        if (!EstadoDonacionSegmentada.EN_TRASLADO.equals(segmento.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede informar una donación que está en camino.");
        }
        return trazabilidad.reportarNoRecibida(idDonacion, idSegmento,
                entidad.getDatosDeEntidad().getRazonSocial(), motivo);
    }

    @GetMapping("/avisos")
    public List<NotificacionesRestClient.Aviso> avisos(@AuthenticationPrincipal Jwt jwt) {
        EntidadBeneficiaria entidad = titular(jwt);
        try {
            return notificaciones.avisosDe(entidad.getDatosDeEntidad().getId());
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudieron cargar los avisos.");
        }
    }

    @GetMapping("/fotos/{idFoto}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> foto(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idFoto) {
        EntidadBeneficiaria entidad = titular(jwt);
        FotoRecepcion foto = fotos.findByIdAndEntidadBeneficiariaId(idFoto, entidad.getIdEntidadBeneficiaria())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(foto.getContentType())).body(foto.getContenido());
    }

    private EntidadBeneficiaria titular(Jwt jwt) {
        String claim = jwt.getClaimAsString("entidad_beneficiaria_id");
        if (claim == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        EntidadBeneficiaria entidad = entidades.find(Long.valueOf(claim));
        if (entidad == null || entidad.getDatosDeEntidad() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return entidad;
    }

    private DonacionSegmentada segmentoPropio(EntidadBeneficiaria entidad, Long idDonacion, Long idSegmento) {
        Donacion donacion = donaciones.findById(idDonacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return donacion.getDonacionesSegmentadas().stream()
                .filter(s -> idSegmento.equals(s.getId())
                        && entidad.getDatosDeEntidad().getId().equals(s.getEntidadBeneficiariaAsignadaId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Segmento segmento(DonacionSegmentada s, List<Long> idsFotos) {
        var sub = s.getSubCategoria();
        var comprobante = s.getComprobanteRecepcionDonacion();
        return new Segmento(s.getId(), s.getEstado().getNombre(),
                sub != null && sub.getCategoria() != null ? sub.getCategoria().name() : null,
                sub != null ? sub.getDescripcion() : null,
                s.getCantidad(),
                sub != null && sub.getUnidad() != null ? sub.getUnidad().name() : null,
                s.getHistorial().stream().sorted(Comparator.comparing(EventoTrazabilidad::getFecha))
                        .map(e -> new Evento(e.getEstadoNuevo().getNombre(), e.getFecha(), e.getDescripcion())).toList(),
                comprobante == null ? null : new Comprobante(comprobante.getId(), comprobante.getFechaEntrega(),
                        comprobante.getDetallesLogistica()),
                idsFotos);
    }

    public record DonacionRecibida(Long id, String descripcion, Date fechaIngreso, String donante, List<Segmento> segmentos) {}
    public record Segmento(Long id, String estado, String categoria, String subcategoria, int cantidad, String unidad,
                           List<Evento> eventos, Comprobante comprobante, List<Long> fotos) {}
    public record Evento(String estado, LocalDateTime fecha, String descripcion) {}
    public record Comprobante(String id, LocalDateTime fechaEntrega, String detalles) {}
}
