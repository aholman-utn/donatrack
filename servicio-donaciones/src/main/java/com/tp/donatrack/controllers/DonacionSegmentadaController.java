package com.tp.donatrack.controllers;

import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
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

    @GetMapping("/{id}/contacto-donante")
    public ResponseEntity<NotificacionRequestDTO> obtenerContactoDonante(@PathVariable("id") Long id) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(id);
        if (segmentada == null) {
            return ResponseEntity.notFound().build();
        }
        Long donanteId = segmentada.getDonanteId();
        Donante donante = donanteService.buscarDonantePorId(donanteId);
        if (donante == null || donante.getPersona() == null || donante.getPersona().getMedioPredeterminado() == null) {
            return ResponseEntity.notFound().build();
        }

        java.util.Map<String, String> mapaMedio = donante.getPersona().getMedioPredeterminado();
        String tipoString = mapaMedio.get("medio");
        String valor = mapaMedio.get("valor");

        if (tipoString == null || valor == null) {
            return ResponseEntity.badRequest().build();
        }

        NotificacionRequestDTO responseDTO = new NotificacionRequestDTO();
        responseDTO.setMedio(com.tp.commons.domain.notificador.TipoNotificador.valueOf(tipoString.toUpperCase()));
        responseDTO.setDestinatario(valor);
        responseDTO.setIdPersona(donante.getPersona().getId());
        responseDTO.setMensaje(null);
        responseDTO.setAsunto(null);

        return ResponseEntity.ok(responseDTO);
    }
}

