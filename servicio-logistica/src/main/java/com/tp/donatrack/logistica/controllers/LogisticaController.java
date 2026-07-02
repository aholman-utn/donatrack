package com.tp.donatrack.logistica.controllers;

import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.services.LogisticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistica")
public class LogisticaController {

    private final LogisticaService logisticaService;

    public LogisticaController(LogisticaService logisticaService) {
        this.logisticaService = logisticaService;
    }

    @PostMapping("/camiones")
    public ResponseEntity<Camion> registrarCamion(@RequestBody Camion camion) {
        return ResponseEntity.ok(logisticaService.registrarCamion(camion));
    }

    @GetMapping("/camiones")
    public ResponseEntity<List<Camion>> listarCamiones() {
        return ResponseEntity.ok(logisticaService.listarCamiones());
    }

    @PostMapping("/choferes")
    public ResponseEntity<Chofer> registrarChofer(@RequestBody Chofer chofer) {
        return ResponseEntity.ok(logisticaService.registrarChofer(chofer));
    }

    @GetMapping("/choferes")
    public ResponseEntity<List<Chofer>> listarChoferes() {
        return ResponseEntity.ok(logisticaService.listarChoferes());
    }

    @PostMapping("/envios")
    public ResponseEntity<Envio> registrarEnvio(@RequestBody Envio envio) {
        return ResponseEntity.ok(logisticaService.registrarEnvio(envio));
    }

    @GetMapping("/envios")
    public ResponseEntity<List<Envio>> listarEnvios() {
        return ResponseEntity.ok(logisticaService.listarEnvios());
    }
    
    @PostMapping("/rutas")
    public ResponseEntity<Ruta> registrarRuta(@RequestBody Ruta ruta) {
        return ResponseEntity.ok(logisticaService.registrarRuta(ruta));
    }

    @GetMapping("/rutas")
    public ResponseEntity<List<Ruta>> listarRutas() {
        return ResponseEntity.ok(logisticaService.listarRutas());
    }

    @PostMapping("/rutas/{id}/iniciar")
    public ResponseEntity<Void> iniciarRuta(@PathVariable("id") Long id) {
        logisticaService.iniciarRuta(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/entregar")
    public ResponseEntity<Void> entregarEnvio(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, String> body) {
        String detalles = (body != null) ? body.get("detalles") : null;
        logisticaService.registrarEntregaExitosa(id, detalles);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/fallar")
    public ResponseEntity<Void> fallarEnvio(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, String> body) {
        String motivo = (body != null) ? body.get("motivo") : null;
        logisticaService.registrarEntregaFallida(id, motivo);
        return ResponseEntity.ok().build();
    }
}
