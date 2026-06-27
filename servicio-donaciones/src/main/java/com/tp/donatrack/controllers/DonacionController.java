package com.tp.donatrack.controllers;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.dtos.DonacionHistorialDTO;
import com.tp.donatrack.services.DonacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donaciones")
public class DonacionController {

    private final DonacionService donacionService;

    public DonacionController(DonacionService donacionService) {
        this.donacionService = donacionService;
    }

    @GetMapping
    public ResponseEntity<List<DonacionHistorialDTO>> listarDonaciones(
            @RequestParam(required = false) Long donanteId) {
        if (donanteId != null) {
            return ResponseEntity.ok(donacionService.obtenerHistorialPorDonante(donanteId));
        }
        return ResponseEntity.ok(donacionService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            DonacionHistorialDTO donacion = donacionService.obtenerPorId(id);
            return ResponseEntity.ok(donacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarDonacion(
            @PathVariable Integer id,
            @RequestBody com.tp.donatrack.dtos.ActualizarDonacionRequest request) {
        try {
            DonacionHistorialDTO actualizada = donacionService.actualizarDonacion(id, request);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar la donación: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarDonacion(@PathVariable Integer id) {
        try {
            donacionService.eliminarDonacion(id);
            return ResponseEntity.ok("Donación eliminada exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<?> crearDonacion(@RequestBody com.tp.donatrack.dtos.CrearDonacionRequest request) {
        try {
            Donacion creada = donacionService.registrarDonacion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear donación: " + e.getMessage());
        }
    }

    @PostMapping("/entregar")
    public ResponseEntity<String> entregarDonacion(@RequestBody EntregaRequest request) {
        try {
            donacionService.registrarEntrega(request.getDonacionSegmentadaId());
            return ResponseEntity.ok("Donación segmentada entregada y evento de incentivos disparado.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @lombok.Getter
    @lombok.Setter
    public static class EntregaRequest {
        private Long donacionSegmentadaId;
    }
}
