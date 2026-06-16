package com.tp.incentivos.controllers;

import com.tp.incentivos.dtos.EntregaDonacionDTO;
import com.tp.incentivos.dtos.PerfilIncentivosDTO;
import com.tp.incentivos.services.IncentivosService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incentivos")
public class IncentivosController {
    private final IncentivosService service;

    public IncentivosController(IncentivosService service) {
        this.service = service;
    }

    @PostMapping("/entrega")
    public ResponseEntity<Void> procesarEntrega(@Valid @RequestBody EntregaDonacionDTO dto) {
        service.procesarNuevaEntrega(dto.getDonanteId(), dto.getEntidadBeneficiariaId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/perfil/{donanteId}")
    public ResponseEntity<PerfilIncentivosDTO> obtenerPerfil(@PathVariable Long donanteId) {
        return ResponseEntity.ok(service.obtenerPerfil(donanteId));
    }
}
