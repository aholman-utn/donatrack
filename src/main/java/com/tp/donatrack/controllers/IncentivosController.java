package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.PerfilIncentivosDTO;
import com.tp.donatrack.services.IncentivosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incentivos")
public class IncentivosController {

    private final IncentivosService incentivosService;

    public IncentivosController(IncentivosService incentivosService) {
        this.incentivosService = incentivosService;
    }

    @GetMapping("/perfil/{donanteId}")
    public ResponseEntity<PerfilIncentivosDTO> obtenerPerfil(@PathVariable Integer donanteId) {
        PerfilIncentivosDTO perfil = incentivosService.obtenerPerfil(donanteId);
        return ResponseEntity.ok(perfil);
    }
}
