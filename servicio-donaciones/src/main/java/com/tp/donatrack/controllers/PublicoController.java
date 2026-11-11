package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.DonacionPublicaDTO;
import com.tp.donatrack.services.DonacionPublicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints públicos (sin token) para la landing y el mapa de entregas.
 * Toda ruta bajo /api/publico/** está habilitada sin autenticación en
 * CommonsSecurityConfig y en el API Gateway.
 */
@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    private final DonacionPublicaService donacionPublicaService;

    public PublicoController(DonacionPublicaService donacionPublicaService) {
        this.donacionPublicaService = donacionPublicaService;
    }

    /** GET /api/publico/donaciones/destacadas?limite=6 */
    @GetMapping("/donaciones/destacadas")
    public ResponseEntity<List<DonacionPublicaDTO>> destacadas(
            @RequestParam(defaultValue = "6") int limite) {
        return ResponseEntity.ok(donacionPublicaService.destacadasUltimoMes(limite));
    }

    /** GET /api/publico/entregas — entregas con coordenadas para el mapa */
    @GetMapping("/entregas")
    public ResponseEntity<List<DonacionPublicaDTO>> entregas() {
        return ResponseEntity.ok(donacionPublicaService.entregasParaMapa());
    }
}
