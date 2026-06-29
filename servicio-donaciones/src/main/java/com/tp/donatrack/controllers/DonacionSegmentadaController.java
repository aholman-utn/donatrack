package com.tp.donatrack.controllers;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.services.DonanteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donaciones-segmentadas")
public class DonacionSegmentadaController {

    private final DonacionRepository donacionRepository;
    private final DonanteService donanteService;

    public DonacionSegmentadaController(
            DonacionRepository donacionRepository,
            DonanteService donanteService
    ) {
        this.donacionRepository = donacionRepository;
        this.donanteService = donanteService;
    }

    @GetMapping("/indicadores/{donacionSegmentadaId}")
    public ResponseEntity<IndicadoresDonanteDTO> obtenerIndicadores(
            @PathVariable("donacionSegmentadaId") Long donacionSegmentadaId,
            @RequestParam("donanteId") Long donanteId,
            @RequestParam("indicadores") List<String> indicadores
    ) {

        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(donacionSegmentadaId);

        IndicadoresDonanteDTO resultado = donanteService.calcularIndicadores(donanteId, segmentada, indicadores);

        return ResponseEntity.ok(resultado);
    }
}
