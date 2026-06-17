package com.tp.incentivos.controllers;

import com.tp.incentivos.dtos.EntregaDonacionDTO;
import com.tp.incentivos.dtos.PerfilIncentivosDTO;
import com.tp.incentivos.dtos.RankingItemDTO;
import com.tp.incentivos.services.IncentivosService;
import com.tp.incentivos.services.ServicioRanking;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incentivos")
public class IncentivosController {

    private final IncentivosService service;
    private final ServicioRanking servicioRanking;

    public IncentivosController(IncentivosService service, ServicioRanking servicioRanking) {
        this.service = service;
        this.servicioRanking = servicioRanking;
    }

    /**
     * Recibe notificación del servicio-donaciones cuando una donación es entregada.
     * Procesa el impacto en métricas y misiones del donante.
     * POST /api/incentivos/entrega
     */
    @PostMapping("/entrega")
    public ResponseEntity<Void> procesarEntrega(@Valid @RequestBody EntregaDonacionDTO dto) {
        service.procesarNuevaEntrega(dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Retorna el perfil completo de incentivos de un donante.
     * GET /api/incentivos/perfil/{donanteId}
     */
    @GetMapping("/perfil/{donanteId}")
    public ResponseEntity<PerfilIncentivosDTO> obtenerPerfil(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerPerfil(donanteId));
    }

    /**
     * Retorna el ranking global de donantes ordenado por donaciones exitosas (DESC).
     * GET /api/incentivos/ranking
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingItemDTO>> obtenerRanking() {
        return ResponseEntity.ok(servicioRanking.obtenerRankingCompleto());
    }

    /**
     * Retorna la posición en el ranking de un donante específico.
     * GET /api/incentivos/ranking/{donanteId}
     */
    @GetMapping("/ranking/{donanteId}")
    public ResponseEntity<RankingItemDTO> obtenerPosicionDonante(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(servicioRanking.obtenerPosicionDonante(donanteId));
    }

    /**
     * Cambia la visibilidad de una insignia en el perfil público del donante.
     * PUT /api/incentivos/perfil/{donanteId}/insignias/{titulo}/visibilidad
     * Body: { "visible": true/false }
     */
    @PutMapping("/perfil/{donanteId}/insignias/{titulo}/visibilidad")
    public ResponseEntity<Void> cambiarVisibilidadInsignia(
            @PathVariable Integer donanteId,
            @PathVariable String titulo,
            @RequestBody Map<String, Boolean> body) {
        Boolean visible = body.get("visible");
        if (visible == null) {
            return ResponseEntity.badRequest().build();
        }
        service.cambiarVisibilidadInsignia(donanteId, titulo, visible);
        return ResponseEntity.ok().build();
    }
}
