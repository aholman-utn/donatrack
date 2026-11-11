package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.necesidad.CrearNecesidadDTO;
import com.tp.donatrack.services.NecesidadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/necesidades/me")
public class NecesidadesPropiasController {
    private final NecesidadService service;
    public NecesidadesPropiasController(NecesidadService service) { this.service = service; }

    private Long entidadId(Jwt jwt) { return Long.valueOf(jwt.getClaimAsString("entidad_beneficiaria_id")); }

    @GetMapping
    public ResponseEntity<?> listar(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.listarPorEntidad(entidadId(jwt)));
    }

    @PostMapping
    public ResponseEntity<?> crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CrearNecesidadDTO dto) {
        dto.setEntidadBeneficiariaId(entidadId(jwt));
        return ResponseEntity.ok(service.crearNecesidad(dto));
    }
}
