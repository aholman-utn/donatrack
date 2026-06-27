package com.tp.donatrack.controllers;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.dtos.CrearEventoRequest;
import com.tp.donatrack.dtos.TrazaDonacionDTO;
import com.tp.donatrack.dtos.TrazaSegmentoDTO;
import com.tp.donatrack.services.DonacionService;
import com.tp.donatrack.services.TrazabilidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trazabilidad")
public class TrazabilidadController {

    private final TrazabilidadService trazabilidadService;

    public TrazabilidadController(TrazabilidadService trazabilidadService) {
        this.trazabilidadService = trazabilidadService;
    }

    // Trazabilizar una donacion completa -> todos sus segmentos
    @GetMapping("/{id}")
    public ResponseEntity<?> trazabilizarDonacion(@PathVariable Integer id) {
        try {
            TrazaDonacionDTO traza = trazabilidadService.trazabilizarDonacion(id);
            return ResponseEntity.ok(traza);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Trazabilizar una donacion segmentada
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

}
