package com.tp.donatrack.controllers;

import com.tp.donatrack.clients.NotificacionesRestClient;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.domain.donacion.ComprobanteRecepcionDonacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;
import com.tp.donatrack.dtos.MetricasActividadDTO;
import com.tp.donatrack.dtos.PerfilDonanteDTO;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.DonanteRepository;
import com.tp.donatrack.services.DonanteService;
import com.tp.donatrack.services.EntidadBeneficiariaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/donantes")
public class PortalDonanteController {
    private final DonanteRepository donantes;
    private final DonacionRepository donaciones;
    private final DonanteService servicio;
    private final EntidadBeneficiariaService entidades;
    private final NotificacionesRestClient notificaciones;

    public PortalDonanteController(DonanteRepository donantes, DonacionRepository donaciones,
            DonanteService servicio, EntidadBeneficiariaService entidades, NotificacionesRestClient notificaciones) {
        this.donantes = donantes;
        this.donaciones = donaciones;
        this.servicio = servicio;
        this.entidades = entidades;
        this.notificaciones = notificaciones;
    }

    @GetMapping("/identidad")
    @Transactional
    public Long identidad(@AuthenticationPrincipal Jwt jwt) {
        List<Donante> coincidencias = donantes.findByEmail(jwt.getSubject());
        if (coincidencias.size() != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Donante donante = coincidencias.getFirst();
        donante.getPersona().setFechaUltimaInteraccion(LocalDateTime.now());
        return donante.getId();
    }

    @GetMapping("/me")
    @Transactional
    public Resumen resumen(@AuthenticationPrincipal Jwt jwt) {
        Donante donante = titular(jwt);
        donante.getPersona().setFechaUltimaInteraccion(LocalDateTime.now());
        boolean humana = donante.getPersona() instanceof PersonaHumana;
        return new Resumen(donante.getNombreCompleto(), humana, servicio.obtenerMetricas(donante.getId()),
                humana ? servicio.obtenerPerfilDonante(donante.getId()) : null,
                donantes.contarDonantesMejorPosicionados(donante.getPerfil().getTotalMisionesCompletadas(),
                        donante.getPerfil().getTotalDonacionesExitosas()) + 1);
    }

    @GetMapping("/me/donaciones")
    @Transactional(readOnly = true)
    public List<DonacionPropia> donaciones(@AuthenticationPrincipal Jwt jwt) {
        return donaciones.findByDonanteId(titular(jwt).getId()).stream()
                .sorted(Comparator.comparing(Donacion::getFechaIngreso, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed())
                .map(d -> new DonacionPropia(d.getId(), d.getDescripcion(), d.getFechaIngreso(),
                        d.getDonacionesSegmentadas().stream().map(this::segmento).toList()))
                .toList();
    }

    @GetMapping("/me/avisos")
    @Transactional(readOnly = true)
    public List<AvisoDonante> avisos(@AuthenticationPrincipal Jwt jwt) {
        Long donanteId = titular(jwt).getId();
        var avisos = new ArrayList<AvisoDonante>();
        var comprobantes = new HashSet<String>();
        for (Donacion donacion : donaciones.findByDonanteId(donanteId)) {
            for (DonacionSegmentada segmento : donacion.getDonacionesSegmentadas()) {
                if (!EstadoDonacionSegmentada.ENTREGADA.equals(segmento.getEstado())) continue;
                var comprobante = segmento.getComprobanteRecepcionDonacion();
                if (comprobante != null && comprobante.getId() != null) comprobantes.add(comprobante.getId());
                var evento = segmento.getUltimoEvento();
                var fecha = comprobante != null && comprobante.getFechaEntrega() != null
                        ? comprobante.getFechaEntrega() : evento == null ? null : evento.getFecha();
                avisos.add(new AvisoDonante(-segmento.getId(), "Tu donación fue entregada",
                        "La entidad beneficiaria recibió el segmento #" + segmento.getId() + " de tu donación.",
                        fecha, donacion.getId()));
            }
        }
        try {
            notificaciones.avisosDe(donanteId).stream()
                    .filter(aviso -> aviso.mensaje() == null || comprobantes.stream()
                            .noneMatch(id -> aviso.mensaje().contains("/api/comprobantes/" + id)))
                    .map(aviso -> new AvisoDonante(aviso.id(), aviso.asunto(), aviso.mensaje(), aviso.fecha(), null))
                    .forEach(avisos::add);
        } catch (RestClientException e) {
            if (avisos.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudieron cargar los avisos.");
            }
        }
        avisos.sort(Comparator.comparing(AvisoDonante::fecha, Comparator.nullsLast(Comparator.reverseOrder())));
        return avisos;
    }

    public record AvisoDonante(Long id, String asunto, String mensaje, LocalDateTime fecha, Long donacionId) {}

    @GetMapping("/me/entidades")
    @Transactional(readOnly = true)
    public List<EntidadDirectorio> directorio(@AuthenticationPrincipal Jwt jwt) {
        titular(jwt);
        return entidades.listarTodos().stream()
                .filter(e -> e.getDatosDeEntidad() != null)
                .map(e -> new EntidadDirectorio(e.getIdEntidadBeneficiaria(), e.getDatosDeEntidad().getRazonSocial(),
                        e.getDatosDeEntidad().getRubro()))
                .toList();
    }

    @PutMapping("/me/insignias")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void visibilidad(@AuthenticationPrincipal Jwt jwt, @RequestBody Visibilidad cambio) {
        Donante donante = titular(jwt);
        if (!(donante.getPersona() instanceof PersonaHumana)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (cambio.visible() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        donante.getPerfil().setVisibilidadInsignia(cambio.visible());
    }

    private Donante titular(Jwt jwt) {
        long id = Long.parseLong(jwt.getClaimAsString("donante_id"));
        List<Donante> coincidencias = donantes.findByEmail(jwt.getSubject());
        if (coincidencias.size() != 1 || coincidencias.getFirst().getId() != id) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return coincidencias.getFirst();
    }

    private Segmento segmento(DonacionSegmentada segmento) {
        var subcategoria = segmento.getSubCategoria();
        var comprobante = segmento.getComprobanteRecepcionDonacion();
        return new Segmento(segmento.getId(), segmento.getEstado().getNombre(),
                subcategoria != null && subcategoria.getCategoria() != null ? subcategoria.getCategoria().name() : null,
                subcategoria != null ? subcategoria.getDescripcion() : null,
                segmento.getHistorial().stream().sorted(Comparator.comparing(EventoTrazabilidad::getFecha))
                        .map(e -> new Evento(e.getEstadoNuevo().getNombre(), e.getFecha(), e.getDescripcion())).toList(),
                comprobante);
    }

    public record Resumen(String nombre, boolean humana, MetricasActividadDTO metricas,
                          PerfilDonanteDTO perfil, long ranking) {}
    public record DonacionPropia(Long id, String descripcion, Date fechaIngreso, List<Segmento> segmentos) {}
    public record Segmento(Long id, String estado, String categoria, String subcategoria,
                           List<Evento> eventos, ComprobanteRecepcionDonacion comprobante) {}
    public record Evento(String estado, LocalDateTime fecha, String descripcion) {}
    public record EntidadDirectorio(Long id, String nombre, String rubro) {}
    public record Visibilidad(Boolean visible) {}
}
