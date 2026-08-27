package com.tp.donatrack.logistica.controllers;

import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistica")
public class LogisticaController {
    private final EnviosService enviosService;
    private final CamionService camionService;
    private final RutaService rutaService;
    private final ChoferService choferService;

    public LogisticaController(
            EnviosService enviosService,
            CamionService camionService,
            RutaService rutaService,
            ChoferService choferService
    ) {
        this.enviosService = enviosService;
        this.camionService = camionService;
        this.rutaService = rutaService;
        this.choferService = choferService;
    }

    @PostMapping("/camiones")
    public ResponseEntity<Camion> registrarCamion(@RequestBody Camion camion) {
        return ResponseEntity.ok(camionService.registrarCamion(camion));
    }

    @GetMapping("/camiones")
    public ResponseEntity<List<Camion>> listarCamiones() {
        return ResponseEntity.ok(camionService.listarCamiones());
    }

    @PostMapping("/choferes")
    public ResponseEntity<Chofer> registrarChofer(@RequestBody Chofer chofer) {
        return ResponseEntity.ok(choferService.registrarChofer(chofer));
    }

    @GetMapping("/choferes")
    public ResponseEntity<List<Chofer>> listarChoferes() {
        return ResponseEntity.ok(choferService.listarChoferes());
    }

    @PostMapping("/envios")
    public ResponseEntity<Envio> registrarEnvio(@RequestBody Envio envio) {
        return ResponseEntity.ok(enviosService.registrarEnvio(envio));
    }

    @PostMapping("/planificar")
    public ResponseEntity<Void> planificarRutas(
            @RequestBody List<DonacionSegmentadaListaParaEntregarALogisticaDTO> loteDonaciones
    ) {
        rutaService.planificarLote(loteDonaciones);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/envios")
    public ResponseEntity<List<Envio>> listarEnvios() {
        return ResponseEntity.ok(enviosService.listarEnvios());
    }

    @PostMapping("/rutas")
    public ResponseEntity<Ruta> registrarRuta(@RequestBody Ruta ruta) {
        return ResponseEntity.ok(rutaService.registrarRuta(ruta));
    }

    @GetMapping("/rutas")
    public ResponseEntity<List<Ruta>> listarRutas() {
        return ResponseEntity.ok(rutaService.listarRutas());
    }

    @PostMapping("/rutas/{id}/iniciar")
    public ResponseEntity<Void> iniciarRuta(@PathVariable("id") Long id) {
        rutaService.iniciarRuta(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/llegada")
    public ResponseEntity<Void> registrarLlegada(@PathVariable("id") Long id) {
        enviosService.registrarLlegadaADestino(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/recibir")
    public ResponseEntity<Void> recibirEnvio(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, String> body) {
        String detalles = (body != null) ? body.get("detalles") : null;
        enviosService.registrarEntregaExitosa(id, detalles);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/fallar")
    public ResponseEntity<Void> fallarEnvio(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, String> body) {
        String motivo = (body != null) ? body.get("motivo") : null;
        enviosService.registrarEntregaFallida(id, motivo);
        return ResponseEntity.ok().build();
    }
}