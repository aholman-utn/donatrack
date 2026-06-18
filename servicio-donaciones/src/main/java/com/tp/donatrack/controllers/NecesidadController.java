package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.necesidad.CrearNecesidadDTO;
import com.tp.donatrack.dtos.necesidad.NecesidadResponseDTO;
import com.tp.donatrack.services.NecesidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/necesidades")
public class NecesidadController {

    private final NecesidadService necesidadService;

    public NecesidadController(NecesidadService necesidadService) {
        this.necesidadService = necesidadService;
    }

    @PostMapping
    public ResponseEntity<NecesidadResponseDTO> crearNecesidad(@Valid @RequestBody CrearNecesidadDTO dto) {
        NecesidadResponseDTO response = necesidadService.crearNecesidad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NecesidadResponseDTO> obtenerPorId(@PathVariable Long id) {
        NecesidadResponseDTO response = necesidadService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/entidad/{entidadId}")
    public ResponseEntity<List<NecesidadResponseDTO>> listarPorEntidad(@PathVariable Long entidadId) {
        List<NecesidadResponseDTO> response = necesidadService.listarPorEntidad(entidadId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NecesidadResponseDTO> actualizarNecesidad(@PathVariable Long id, @Valid @RequestBody CrearNecesidadDTO dto) {
        NecesidadResponseDTO response = necesidadService.actualizarNecesidad(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNecesidad(@PathVariable Long id) {
        necesidadService.eliminarNecesidad(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
