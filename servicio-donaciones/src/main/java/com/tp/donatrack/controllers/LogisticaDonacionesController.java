package com.tp.donatrack.controllers;

import com.tp.commons.dtos.logistica.SegmentoLogistico;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.repositories.DonacionSegmentadaRepository;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interno/logistica")
public class LogisticaDonacionesController {
    private final DonacionSegmentadaRepository segmentos;
    private final EntidadBeneficiariaRepository entidades;

    public LogisticaDonacionesController(DonacionSegmentadaRepository segmentos, EntidadBeneficiariaRepository entidades) {
        this.segmentos = segmentos;
        this.entidades = entidades;
    }

    @GetMapping("/disponibles")
    @Transactional(readOnly = true)
    public List<SegmentoLogistico> disponibles() {
        return datos(segmentos.findByEstadoAndRutaLogisticaIdIsNullOrderByIdAsc(EstadoDonacionSegmentada.ASIGNACION_REALIZADA))
                .stream().filter(s -> s.direccion() != null && !s.direccion().isBlank()).toList();
    }

    @PostMapping("/estados")
    @Transactional(readOnly = true)
    public List<SegmentoLogistico> estados(@RequestBody List<String> rutas) {
        return rutas.isEmpty() ? List.of() : datos(segmentos.findByRutaLogisticaIdIn(rutas));
    }

    @PostMapping("/{ruta}/reservar")
    @Transactional
    public List<SegmentoLogistico> reservar(@PathVariable String ruta, @RequestBody List<Long> ids) {
        List<DonacionSegmentada> seleccion = bloquear(ids);
        List<SegmentoLogistico> datos = datos(seleccion);
        for (int i = 0; i < seleccion.size(); i++) {
            DonacionSegmentada segmento = seleccion.get(i);
            if (ruta.equals(segmento.getRutaLogisticaId())) continue;
            if (segmento.getRutaLogisticaId() != null || !segmento.getEstado().equals(EstadoDonacionSegmentada.ASIGNACION_REALIZADA)) {
                throw conflicto("El segmento #" + segmento.getId() + " ya no está disponible.");
            }
            if (datos.get(i).direccion() == null || datos.get(i).direccion().isBlank()) {
                throw conflicto("El segmento #" + segmento.getId() + " no tiene dirección de entrega.");
            }
            segmento.setRutaLogisticaId(ruta);
            segmento.listarParaEntrega("Logística");
            segmento.solicitarPlanificacion("Logística");
        }
        return datos;
    }

    @PostMapping("/{ruta}/iniciar")
    @Transactional
    public void iniciar(@PathVariable String ruta, @RequestBody List<Long> ids) {
        for (DonacionSegmentada segmento : bloquear(ids)) {
            validarRuta(ruta, segmento);
            if (segmento.getEstado().equals(EstadoDonacionSegmentada.EN_PLANIFICACION)) {
                segmento.iniciarTraslado("Logística");
            }
        }
    }

    @PostMapping("/{ruta}/{id}/llegada")
    @Transactional
    public void llegada(@PathVariable String ruta, @PathVariable Long id) {
        DonacionSegmentada segmento = bloquear(List.of(id)).getFirst();
        validarRuta(ruta, segmento);
        if (segmento.getLlegadaLogistica() != null) return;
        if (!segmento.getEstado().equals(EstadoDonacionSegmentada.EN_TRASLADO)) {
            throw conflicto("Iniciá el recorrido antes de registrar la llegada.");
        }
        segmento.setLlegadaLogistica(LocalDateTime.now());
        segmento.registrarLlegadaADestino("Logística");
    }

    private List<DonacionSegmentada> bloquear(List<Long> ids) {
        return ids.stream().distinct().sorted()
                .map(id -> segmentos.bloquear(id).orElseThrow(() -> conflicto("No existe el segmento #" + id)))
                .toList();
    }

    private void validarRuta(String ruta, DonacionSegmentada segmento) {
        if (!ruta.equals(segmento.getRutaLogisticaId())) throw conflicto("El segmento pertenece a otra ruta.");
    }

    private List<SegmentoLogistico> datos(List<DonacionSegmentada> seleccion) {
        var ids = seleccion.stream().map(DonacionSegmentada::getEntidadBeneficiariaAsignadaId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, EntidadBeneficiaria> porPersona = entidades.findAllByDatosDeEntidadIdIn(ids).stream()
                .collect(Collectors.toMap(e -> e.getDatosDeEntidad().getId(), Function.identity()));
        return seleccion.stream().map(s -> {
            EntidadBeneficiaria entidad = porPersona.get(s.getEntidadBeneficiariaAsignadaId());
            var direccion = entidad == null ? null : entidad.getDatosDeEntidad().getDireccion();
            var unidad = s.getSubCategoria() == null ? null : s.getSubCategoria().getUnidad();
            return new SegmentoLogistico(s.getId(), s.getEntidadBeneficiariaAsignadaId(),
                    s.getEntidadBeneficiariaNombre(), direccion == null ? null : direccion.getDireccion(),
                    s.getCantidad(), unidad == null ? null : unidad.name(), s.getEstado().getNombre(),
                    s.getRutaLogisticaId(), s.getLlegadaLogistica());
        }).toList();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ProblemDetail error(ResponseStatusException error) {
        return error.getBody();
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
