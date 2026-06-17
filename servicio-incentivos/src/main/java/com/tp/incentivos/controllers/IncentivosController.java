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
@RequestMapping("/perfil")
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
     * POST /perfil/entrega
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
    @GetMapping("/{donanteId}")
    public ResponseEntity<PerfilIncentivosDTO> obtenerPerfil(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerPerfil(donanteId));
    }

    @GetMapping("/misiones/{donanteId}")
    public ResponseEntity<MisionesDonanteDTO> obtenerMisiones(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerMisiones(donanteId));
    }

    @GetMapping("/insignias/{donanteId}")
    public ResponseEntity<InsigniasDonanteDTO> obtenerInsignias(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerInsignias(donanteId));
    }

    @GetMapping("/metricas/{donanteId}")
    public ResponseEntity<MetricasActividadDTO> obtenerMetricas(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(service.obtenerMetricas(donanteId));
    }

    /**
     * Retorna el ranking global de donantes ordenado por donaciones exitosas
     * (DESC).
     * GET /incentivos/ranking
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingItemDTO>> obtenerRanking() {
        return ResponseEntity.ok(servicioRanking.obtenerRankingCompleto());
    }

    /**
     * Retorna la posición en el ranking de un donante específico.
     * GET /incentivos/ranking/{donanteId}
     */
    @GetMapping("/ranking/{donanteId}")
    public ResponseEntity<RankingItemDTO> obtenerPosicionDonante(@PathVariable Integer donanteId) {
        return ResponseEntity.ok(servicioRanking.obtenerPosicionDonante(donanteId));
    }

}
