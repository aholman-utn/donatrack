package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.CrearEventoRequest;
import com.tp.donatrack.dtos.TrazaDonacionDTO;
import com.tp.donatrack.dtos.TrazaSegmentoDTO;
import com.tp.donatrack.services.TrazabilidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para la trazabilidad de donaciones segmentadas.
 * Expone endpoints para consultar el historial de estados y realizar transiciones.
 */
@RestController
@RequestMapping("/trazabilidad")
public class TrazabilidadController {

    private final TrazabilidadService trazabilidadService;

    public TrazabilidadController(TrazabilidadService trazabilidadService) {
        this.trazabilidadService = trazabilidadService;
    }

    /**
     * Obtiene la trazabilidad completa de una donación, incluyendo todos sus segmentos.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> trazabilizarDonacion(@PathVariable Integer id) {
        try {
            TrazaDonacionDTO traza = trazabilidadService.trazabilizarDonacion(id);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Obtiene la trazabilidad de un segmento específico de una donación.
     */
    @GetMapping("/{idDonacion}/{idSegmento}")
    public ResponseEntity<?> trazabilizarDonacionSegmentada(
            @PathVariable Integer idDonacion,
            @PathVariable Integer idSegmento
    ) {
        try {
            TrazaSegmentoDTO traza = trazabilidadService.trazabilizarDonacionSegmentada(idDonacion, idSegmento);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Realiza una transición genérica de estado sobre una donación segmentada.
     */
    @PostMapping("/{idDonacion}/{idSegmento}/transicionar")
    public ResponseEntity<?> transicionarDonacion(
        @PathVariable Integer idDonacion,
        @PathVariable Integer idSegmento,
        @RequestBody CrearEventoRequest request
    ) {
        try {
            TrazaSegmentoDTO traza = trazabilidadService.transicionarDonacion(idDonacion, idSegmento, request);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Marca una donación segmentada como lista para entregar (ruta planificada).
     */
    @PostMapping("/{idDonacion}/{idSegmento}/transicionar/lista_entregar")
    public ResponseEntity<?> transicionarListaEntregar(
        @PathVariable Integer idDonacion,
        @PathVariable Integer idSegmento,
        @RequestParam String actor
    ) {
        try {
            TrazaSegmentoDTO traza = trazabilidadService.transicionListaEntregar(idDonacion, idSegmento, actor);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Transiciona una donación segmentada a estado EN_TRASLADO (camión inició recorrido).
     */
    @PostMapping("/{idDonacion}/{idSegmento}/transicionar/en_traslado")
    public ResponseEntity<?> transicionarEnTraslado(
        @PathVariable Integer idDonacion,
        @PathVariable Integer idSegmento,
        @RequestParam String actor
    ) {
        try {
            TrazaSegmentoDTO traza = trazabilidadService.transicionEnTraslado(idDonacion, idSegmento, actor);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Registra una entrega fallida con justificación. La donación vuelve al depósito.
     */
    @PostMapping("/{idDonacion}/{idSegmento}/transicionar/entrega_fallida")
    public ResponseEntity<?> transicionarEntregaFallida(
        @PathVariable Integer idDonacion,
        @PathVariable Integer idSegmento,
        @RequestParam String actor,
        @RequestParam String justificacion
    ) {
        try {
            TrazaSegmentoDTO traza = trazabilidadService.transicionEntregaFallida(idDonacion, idSegmento, actor, justificacion);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Marca una donación segmentada como vencida por decisión de un administrador.
     */
    @PostMapping("/{idDonacion}/{idSegmento}/transicionar/marcar_vencida")
    public ResponseEntity<?> transicionarMarcarVencida(
            @PathVariable Integer idDonacion,
            @PathVariable Integer idSegmento,
            @RequestParam String actor
    ) {
        try {
            TrazaSegmentoDTO traza = trazabilidadService.transicionMarcarVencida(idDonacion, idSegmento, actor);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Permite a la entidad beneficiaria confirmar la recepción de una donación.
     * Transiciona a ENTREGADA y dispara notificación de entrega exitosa.
     */
    @PatchMapping("/{idDonacion}/{idSegmento}/recepcionar")
    public ResponseEntity<?> recepcionarEntrega(
            @PathVariable Integer idDonacion,
            @PathVariable Integer idSegmento
    ) {
        try {
            TrazaSegmentoDTO traza = trazabilidadService.recepcionarEntrega(idDonacion, idSegmento);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
