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

    @GetMapping("/donante/{donanteId}")
    public ResponseEntity<List<DonacionHistorialDTO>> obtenerHistorialPorDonante(@PathVariable Integer donanteId) {
        List<DonacionHistorialDTO> historial = donacionService.obtenerHistorialPorDonante(donanteId);
        return ResponseEntity.ok(historial);
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

    @PostMapping("/test-crear/{donanteId}")
    public ResponseEntity<String> crearDonacionPrueba(@PathVariable Integer donanteId) {
        try {
            donacionService.registrarDonacionPrueba(donanteId);
            return ResponseEntity.ok("Donación de prueba creada exitosamente para el donante " + donanteId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/test-entregar")
    public ResponseEntity<String> entregarDonacionPrueba(@RequestBody EntregaRequest request) {
        try {
            donacionService.confirmarEntregaPrueba(request.getDonanteId(), request.getEntidadBeneficiariaId());
            return ResponseEntity.ok("Donación/es entregadas y evento de incentivos disparado.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @lombok.Getter
    @lombok.Setter
    public static class EntregaRequest {
        private Integer donanteId;
        private Integer entidadBeneficiariaId;
    }
}
