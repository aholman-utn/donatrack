package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.AsignarDonacionDTO;
import com.tp.donatrack.dtos.ResultadoMatchmakingDTO;
import com.tp.donatrack.routes.MatchmakingRoutes;
import com.tp.donatrack.services.MatchmakingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(MatchmakingRoutes.BASE)
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    /**
     * Obtiene el ranking de entidades beneficiarias para una donación segmentada
     * específica.
     * GET /matchmaking/ranking?donacionSegmentadaId=1
     */
    @GetMapping(MatchmakingRoutes.RANKING)
    public ResponseEntity<?> obtenerRanking(@RequestParam Integer donacionSegmentadaId) {
        try {
            ResultadoMatchmakingDTO resultado = matchmakingService.obtenerRanking(donacionSegmentadaId);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtiene el ranking de matchmaking para TODAS las donaciones en depósito de un
     * donante.
     * GET /matchmaking/ranking/donante/{donanteId}
     */
    @GetMapping(MatchmakingRoutes.RANKING + "/donante/{donanteId}")
    public ResponseEntity<?> obtenerRankingPorDonante(@PathVariable Integer donanteId) {
        try {
            List<ResultadoMatchmakingDTO> resultados = matchmakingService.obtenerRankingPorDonante(donanteId);
            return ResponseEntity.ok(resultados);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Asigna una donación segmentada a la entidad beneficiaria seleccionada del
     * ranking.
     * POST /matchmaking/asignar
     * Body: { "donacionSegmentadaId": 1, "entidadBeneficiariaId": 2 }
     */
    @PostMapping(MatchmakingRoutes.ASIGNAR)
    public ResponseEntity<String> asignarDonacion(@Valid @RequestBody AsignarDonacionDTO request) {
        try {
            matchmakingService.asignarDonacion(
                    request.getDonacionSegmentadaId(),
                    request.getEntidadBeneficiariaId());
            return ResponseEntity.ok("Donación segmentada asignada exitosamente a la entidad beneficiaria.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
