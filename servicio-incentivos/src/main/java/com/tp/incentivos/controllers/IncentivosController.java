package com.tp.incentivos.controllers;

import com.tp.incentivos.dtos.EntregaDonacionDTO;
import com.tp.incentivos.dtos.InsigniasDonanteDTO;
import com.tp.incentivos.dtos.MetricasActividadDTO;
import com.tp.incentivos.dtos.MisionesDonanteDTO;
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
@RequestMapping("/incentivos")
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
     * POST /incentivos/entrega
     */
    @PostMapping("/entrega")
    public ResponseEntity<Void> procesarEntrega(@Valid @RequestBody EntregaDonacionDTO dto) {
        service.procesarNuevaEntrega(dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Retorna el perfil completo de incentivos de un donante.
     * GET /incentivos/perfil/{donanteId}
     */
    @GetMapping("/perfil/{donanteId}")
    public ResponseEntity<PerfilIncentivosDTO> obtenerPerfil(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerPerfil(donanteId));
    }

    /**
     * Retorna la misión actual y las próximas misiones de un donante.
     * GET /incentivos/misiones/{donanteId}
     */
    @GetMapping("/misiones/{donanteId}")
    public ResponseEntity<MisionesDonanteDTO> obtenerMisiones(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerMisiones(donanteId));
    }

    /**
     * Retorna las insignias ganadas de un donante.
     * GET /incentivos/insignias/{donanteId}
     */
    @GetMapping("/insignias/{donanteId}")
    public ResponseEntity<InsigniasDonanteDTO> obtenerInsignias(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerInsignias(donanteId));
    }

    /**
     * Retorna las métricas de actividad de un donante (acumuladas y período actual).
     * GET /incentivos/metricas/{donanteId}
     */
    @GetMapping("/metricas/{donanteId}")
    public ResponseEntity<MetricasActividadDTO> obtenerMetricas(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerMetricas(donanteId));
    }

    /**
     * Retorna el ranking global de donantes ordenado por misiones completadas.
     * GET /incentivos/ranking
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingItemDTO>> obtenerRanking(
            @RequestParam(value = "mesActual", required = false, defaultValue = "false") boolean mesActual) {
        return ResponseEntity.ok(servicioRanking.obtenerRankingCompleto(mesActual));
    }

    /**
     * Retorna la posición en el ranking de un donante específico.
     * GET /incentivos/ranking/{donanteId}
     */
    @GetMapping("/ranking/{donanteId}")
    public ResponseEntity<RankingItemDTO> obtenerPosicionDonante(
            @PathVariable Integer donanteId,
            @RequestParam(value = "mesActual", required = false, defaultValue = "false") boolean mesActual) {
        return ResponseEntity.ok(servicioRanking.obtenerPosicionDonante(donanteId, mesActual));
    }

}
