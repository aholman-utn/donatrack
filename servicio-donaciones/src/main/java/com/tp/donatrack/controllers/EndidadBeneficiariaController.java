package com.tp.donatrack.controllers;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.dtos.CrearPersonaJuridicaRequest;
import com.tp.donatrack.services.EntidadBeneficiariaService;
import com.tp.donatrack.routes.EntidadBeneficiariaRoutes;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping(EntidadBeneficiariaRoutes.BASE)
public class EndidadBeneficiariaController {
    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public EndidadBeneficiariaController(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    @PostMapping()
    public ResponseEntity<EntidadBeneficiaria> crearEntidad(
            @Valid @RequestBody CrearPersonaJuridicaRequest request) {

        EntidadBeneficiaria entidad = entidadBeneficiariaService.registrar(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(entidad);
    }

    @GetMapping()
    public ResponseEntity<List<EntidadBeneficiaria>> listarTodos() {
        return ResponseEntity.ok(entidadBeneficiariaService.listarTodos());
    }

    @GetMapping(EntidadBeneficiariaRoutes.POR_ID)
    public ResponseEntity<EntidadBeneficiaria> buscarEntidad(@PathVariable Long id) {
        EntidadBeneficiaria entidad = entidadBeneficiariaService.buscarEntidad(id);
        if (entidad == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entidad);
    }

    @PutMapping(EntidadBeneficiariaRoutes.POR_ID)
    public ResponseEntity<EntidadBeneficiaria> actualizarDatosPerosnales(
         @PathVariable Long id,
         @Valid @RequestBody CrearPersonaJuridicaRequest request) {

        EntidadBeneficiaria actualizado = entidadBeneficiariaService.actualizarDatosPerosnales(id, request.toDomain());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping(EntidadBeneficiariaRoutes.POR_ID)
    public ResponseEntity<EntidadBeneficiaria> eliminar(@PathVariable Long id) {
        entidadBeneficiariaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
